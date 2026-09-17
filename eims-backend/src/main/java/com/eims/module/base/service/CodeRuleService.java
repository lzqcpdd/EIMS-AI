package com.eims.module.base.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eims.common.exception.BusinessException;
import com.eims.module.base.entity.CodeRule;
import com.eims.module.base.mapper.CodeRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CodeRuleService {

    private final CodeRuleMapper codeRuleMapper;

    @Transactional
    public synchronized String generateCode(String ruleCode) {
        CodeRule rule = codeRuleMapper.selectOne(new LambdaQueryWrapper<CodeRule>()
                .eq(CodeRule::getRuleCode, ruleCode)
                .eq(CodeRule::getIsDeleted, 0));
        if (rule == null) {
            throw new BusinessException("编码规则不存在: " + ruleCode);
        }
        int seq = (rule.getCurrentSeq() == null ? 0 : rule.getCurrentSeq()) + 1;
        rule.setCurrentSeq(seq);
        codeRuleMapper.updateById(rule);

        String datePart = DateUtil.format(new Date(), rule.getDatePattern().replace("yyyy", "yyyy").replace("MM", "MM").replace("dd", "dd"));
        String seqStr = String.format("%0" + rule.getSeqLength() + "d", seq);
        return rule.getPrefix() + datePart + seqStr;
    }
}
