package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.entity.Sequence;
import com.office.supplies.mapper.SequenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SequenceGeneratorService extends ServiceImpl<SequenceMapper, Sequence> {

    private static final Logger logger = LoggerFactory.getLogger(SequenceGeneratorService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int MAX_RETRY = 3;

    @Resource
    private SequenceMapper sequenceMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String generateNo(String prefix, String seqName) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        int sequenceValue = getNextSequence(seqName, date);
        return prefix + date + String.format("%04d", sequenceValue);
    }

    private int getNextSequence(String seqName, String date) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                Sequence seq = sequenceMapper.selectBySeqNameAndDateForUpdate(seqName, date);
                if (seq == null) {
                    seq = new Sequence();
                    seq.setSeqName(seqName);
                    seq.setSeqDate(date);
                    seq.setCurrentValue(1);
                    seq.setMaxValue(9999);
                    sequenceMapper.insert(seq);
                    return 1;
                }

                int currentValue = seq.getCurrentValue();
                if (currentValue >= seq.getMaxValue()) {
                    currentValue = 1;
                }

                int rows = sequenceMapper.incrementValue(seq.getId(), seq.getVersion());
                if (rows > 0) {
                    return currentValue;
                }

                retryCount++;
                if (retryCount < MAX_RETRY) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Sequence generation interrupted", e);
            } catch (Exception e) {
                retryCount++;
                logger.warn("Sequence generation retry {} for {}: {}", retryCount, seqName, e.getMessage());
                if (retryCount >= MAX_RETRY) {
                    throw new RuntimeException("Failed to generate sequence after " + MAX_RETRY + " retries", e);
                }
            }
        }
        throw new RuntimeException("Failed to generate sequence: " + seqName);
    }
}
