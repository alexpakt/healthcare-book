package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.GrowthAlertRule;
import com.healthcare.childrecords.entity.GrowthAlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthAlertRuleRepository extends JpaRepository<GrowthAlertRule, Long> {

    List<GrowthAlertRule> findByRuleTypeAndEnabledTrue(GrowthAlertType ruleType);
}
