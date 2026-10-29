package com.healthcare.childrecords.config;

import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.repository.ChildRepository;
import com.healthcare.childrecords.repository.DoctorRepository;
import com.healthcare.childrecords.util.AmkaHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Recomputes amka_hash with the keyed HMAC for rows still holding the legacy plain SHA-256 hash.
 * The AMKA can only be read after AES decryption, so this cannot be done in a SQL/Liquibase changeset.
 * Idempotent: rows whose hash already matches are left untouched, so it is cheap after the first run.
 * <p>
 * Rows that share an AMKA would collide on the unique amka_hash index once rehashed. Such duplicates are
 * detected up front and reported by id (never by AMKA) before anything is written.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AmkaHashMigrationRunner implements ApplicationRunner {

    private final ChildRepository childRepository;
    private final DoctorRepository doctorRepository;
    private final AmkaHashUtil amkaHashUtil;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> {
            List<Child> children = childRepository.findAll();
            List<Doctor> doctors = doctorRepository.findAll();

            List<String> duplicates = new ArrayList<>();
            duplicates.addAll(findDuplicates("children", children, Child::getAmka, Child::getId));
            duplicates.addAll(findDuplicates("doctors", doctors, Doctor::getAmka, Doctor::getId));
            if (!duplicates.isEmpty()) {
                duplicates.forEach(d -> log.error("Duplicate AMKA detected: {}", d));
                throw new IllegalStateException(
                        "AMKA hash migration aborted, nothing was changed. Rows sharing an AMKA must be "
                                + "resolved first: " + String.join("; ", duplicates));
            }

            int rehashedChildren = rehash(children, Child::getAmka, Child::getAmkaHash, Child::setAmkaHash);
            int rehashedDoctors = rehash(doctors, Doctor::getAmka, Doctor::getAmkaHash, Doctor::setAmkaHash);
            if (rehashedChildren + rehashedDoctors > 0) {
                log.info("Migrated AMKA hashes to HMAC-SHA256: {} children, {} doctors",
                        rehashedChildren, rehashedDoctors);
            }
        });
    }

    private <T> List<String> findDuplicates(String table, List<T> rows,
                                            Function<T, String> amka, Function<T, Long> id) {
        Map<String, List<Long>> idsByHash = new HashMap<>();
        for (T row : rows) {
            String hash = amkaHashUtil.hashAmka(amka.apply(row));
            if (hash != null) {
                idsByHash.computeIfAbsent(hash, h -> new ArrayList<>()).add(id.apply(row));
            }
        }
        return idsByHash.values().stream()
                .filter(ids -> ids.size() > 1)
                .map(ids -> table + " ids " + ids)
                .toList();
    }

    private <T> int rehash(List<T> rows, Function<T, String> amka,
                           Function<T, String> currentHash, BiConsumer<T, String> setHash) {
        int changed = 0;
        for (T row : rows) {
            String hash = amkaHashUtil.hashAmka(amka.apply(row));
            if (hash != null && !hash.equals(currentHash.apply(row))) {
                setHash.accept(row, hash);
                changed++;
            }
        }
        return changed;
    }
}
