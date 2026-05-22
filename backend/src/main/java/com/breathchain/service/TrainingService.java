package com.breathchain.service;

import com.breathchain.dto.TrainingCompleteDTO;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.vo.TrainingResultVO;

import java.util.List;

public interface TrainingService {

    /** 完成一次训练，落库 + 触发上链 + 派发奖励 */
    TrainingResultVO completeTraining(Long userId, Long taskId, TrainingCompleteDTO dto);

    List<TrainingRecord> myHistory(Long userId);

    /** 医生端：查看某患者的训练历史 */
    List<TrainingRecord> patientHistory(Long doctorId, Long patientId);

    /** 计算用户当前的连续打卡天数（含今天） */
    int streakDays(Long userId);
}
