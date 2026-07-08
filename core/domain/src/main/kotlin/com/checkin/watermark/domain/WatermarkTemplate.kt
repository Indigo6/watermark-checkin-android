package com.checkin.watermark.domain

enum class WatermarkTemplate(
    val title: String,
) {
    SimpleCheckin("简洁打卡"),
    EvidenceCheckin("证据打卡"),
    WorkCheckin("工作打卡"),
}
