package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.CareActivityMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.CarePlanMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.CarePlanRuleMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.PlantMapper;
import cn.skylark.xiaolvxingqiu.boot.model.CareActivity;
import cn.skylark.xiaolvxingqiu.boot.model.CareActivityEntity;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanRuleEntity;
import cn.skylark.xiaolvxingqiu.boot.model.CareTask;
import cn.skylark.xiaolvxingqiu.boot.model.PlantAlbumItem;
import cn.skylark.xiaolvxingqiu.boot.model.PlantAlbumPage;
import cn.skylark.xiaolvxingqiu.boot.model.PlantCareStats;
import cn.skylark.xiaolvxingqiu.boot.model.PlantMonthlyCount;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import cn.skylark.xiaolvxingqiu.boot.model.PlantGrowthRecord;
import cn.skylark.xiaolvxingqiu.boot.model.PlantGrowthRecordPage;
import cn.skylark.xiaolvxingqiu.boot.model.PlantStatusLogEntry;
import cn.skylark.xiaolvxingqiu.boot.model.PlantStatusTimeDistributionItem;
import cn.skylark.xiaolvxingqiu.boot.model.WateringTimeSegmentCount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CareActivityService {

    private final CareActivityMapper careActivityMapper;
    private final CarePlanRuleMapper carePlanRuleMapper;
    private final CarePlanMapper carePlanMapper;
    private final PlantMapper plantMapper;
    private final CareMetaService careMetaService;
    private final ObjectMapper objectMapper;

    public CareActivityService(CareActivityMapper careActivityMapper,
                               CarePlanRuleMapper carePlanRuleMapper,
                               CarePlanMapper carePlanMapper,
                               PlantMapper plantMapper,
                               CareMetaService careMetaService,
                               ObjectMapper objectMapper) {
        this.careActivityMapper = careActivityMapper;
        this.carePlanRuleMapper = carePlanRuleMapper;
        this.carePlanMapper = carePlanMapper;
        this.plantMapper = plantMapper;
        this.careMetaService = careMetaService;
        this.objectMapper = objectMapper;
    }

    public List<CareTask> listTasks(Long userId) {
        return listTasks(userId, null);
    }

    public List<CareTask> listTasks(Long userId, Long gardenId) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(3);
        LocalDate cursor = today;
        while (!cursor.isAfter(horizon)) {
            generateActivitiesForDate(userId, cursor);
            cursor = cursor.plusDays(1);
        }
        List<CareActivityEntity> rows = gardenId == null
                ? careActivityMapper.selectByDateRange(userId, today, horizon)
                : careActivityMapper.selectByDateRangeWithGarden(userId, today, horizon, gardenId);
        List<CareTask> tasks = new ArrayList<>();
        for (CareActivityEntity row : rows) {
            CareTask task = new CareTask();
            task.setId(String.valueOf(row.getId()));
            task.setName(careMetaService.label(row.getActivityType()));
            task.setPlantName(row.getPlantName());
            int offset = (int) ChronoUnit.DAYS.between(today, row.getScheduledDate());
            task.setOffset(offset);
            task.setTimeText(dayText(offset) + " 09:00");
            task.setIcon(careMetaService.icon(row.getActivityType()));
            task.setCompleted("COMPLETED".equalsIgnoreCase(row.getStatus()));
            tasks.add(task);
        }
        return tasks;
    }

    @Transactional
    public CareTask completeTask(Long userId, String taskId, Map<String, Object> record) {
        Long activityId = Long.parseLong(taskId);
        CareActivityEntity target = careActivityMapper.selectByUserIdAndId(userId, activityId);
        if (target == null) {
            throw new IllegalArgumentException("task not found: " + taskId);
        }
        String recordJson = null;
        if (record != null && !record.isEmpty()) {
            try {
                recordJson = objectMapper.writeValueAsString(record);
            } catch (Exception ignore) {
                recordJson = null;
            }
        }
        careActivityMapper.complete(userId, activityId, recordJson);
        CareTask task = new CareTask();
        task.setId(taskId);
        task.setName(careMetaService.label(target.getActivityType()));
        task.setPlantName(target.getPlantName());
        int offset = (int) ChronoUnit.DAYS.between(LocalDate.now(), target.getScheduledDate());
        task.setOffset(offset);
        task.setTimeText(dayText(offset) + " " + LocalDateTime.now().toLocalTime().toString());
        task.setIcon(careMetaService.icon(target.getActivityType()));
        task.setCompleted(true);
        return task;
    }

    public List<CareActivity> listActivitiesByDate(Long userId, String date) {
        return listActivitiesByDate(userId, date, null);
    }

    public List<CareActivity> listActivitiesByDate(Long userId, String date, Long gardenId) {
        LocalDate targetDate = LocalDate.parse(date);
        generateActivitiesForDate(userId, targetDate);
        List<CareActivityEntity> rows = gardenId == null
                ? careActivityMapper.selectByDate(userId, targetDate)
                : careActivityMapper.selectByDateWithGarden(userId, targetDate, gardenId);
        return convertActivityRows(rows);
    }

    public List<CareActivity> listActivitiesByMonth(Long userId, String month) {
        return listActivitiesByMonth(userId, month, null);
    }

    public List<CareActivity> listActivitiesByMonth(Long userId, String month, Long gardenId) {
        List<CareActivityEntity> rows = gardenId == null
                ? careActivityMapper.selectByMonth(userId, month)
                : careActivityMapper.selectByMonthWithGarden(userId, month, gardenId);
        return convertActivityRows(rows);
    }

    public PlantGrowthRecordPage listPlantGrowthRecords(Long userId, Long plantId, Integer pageNo, Integer pageSize) {
        int safePageNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int safePageSize = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 50);
        int offset = (safePageNo - 1) * safePageSize;

        Plant plant = plantMapper.selectByUserIdAndId(userId, plantId);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + plantId);
        }

        long total = careActivityMapper.countCompletedByPlant(userId, plantId);
        List<CareActivityEntity> rows = careActivityMapper.selectCompletedByPlantPaged(userId, plantId, offset, safePageSize);
        List<PlantGrowthRecord> list = new ArrayList<>();
        for (CareActivityEntity row : rows) {
            PlantGrowthRecord item = new PlantGrowthRecord();
            item.setId(String.valueOf(row.getId()));
            item.setName(careMetaService.label(row.getActivityType()));
            item.setIcon(careMetaService.icon(row.getActivityType()));
            LocalDateTime doneAt = row.getCompletedAt();
            item.setDate(doneAt != null ? doneAt.toLocalDate().toString() : row.getScheduledDate().toString());
            item.setTimeText(doneAt != null ? doneAt.toLocalTime().toString() : "09:00");
            item.setContent(buildRecordText(row.getActivityType(), parseRecord(row.getRecordJson())));
            list.add(item);
        }

        PlantGrowthRecordPage page = new PlantGrowthRecordPage();
        page.setPageNo(safePageNo);
        page.setPageSize(safePageSize);
        page.setTotal(total);
        page.setHasMore((long) safePageNo * safePageSize < total);
        page.setList(list);
        return page;
    }

    public PlantAlbumPage listPlantAlbumRecords(Long userId, Long plantId, Integer pageNo, Integer pageSize) {
        int safePageNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int safePageSize = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 50);
        int offset = (safePageNo - 1) * safePageSize;

        Plant plant = plantMapper.selectByUserIdAndId(userId, plantId);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + plantId);
        }

        long total = careActivityMapper.countCompletedPhotosByPlant(userId, plantId);
        List<CareActivityEntity> rows = careActivityMapper.selectCompletedPhotosByPlantPaged(userId, plantId, offset, safePageSize);
        List<PlantAlbumItem> list = new ArrayList<>();
        for (CareActivityEntity row : rows) {
            Map<String, Object> record = parseRecord(row.getRecordJson());
            List<String> images = extractPhotoUrls(record);
            if (images.isEmpty()) {
                continue;
            }
            PlantAlbumItem item = new PlantAlbumItem();
            item.setId(String.valueOf(row.getId()));
            LocalDateTime doneAt = row.getCompletedAt();
            item.setDate(doneAt != null ? doneAt.toLocalDate().toString() : row.getScheduledDate().toString());
            String desc = firstNonBlank(record, "note", "remark", "description", "desc", "content", "text");
            item.setDesc(desc.isEmpty() ? "拍照记录" : desc);
            item.setImages(images);
            list.add(item);
        }

        PlantAlbumPage page = new PlantAlbumPage();
        page.setPageNo(safePageNo);
        page.setPageSize(safePageSize);
        page.setTotal(total);
        page.setHasMore((long) safePageNo * safePageSize < total);
        page.setList(list);
        return page;
    }

    public PlantCareStats getPlantCareStats(Long userId, Long plantId) {
        Plant plant = plantMapper.selectByUserIdAndId(userId, plantId);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + plantId);
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        long total = careActivityMapper.countCompletedByPlant(userId, plantId);
        long photos = careActivityMapper.countCompletedPhotosByPlant(userId, plantId);
        long water = careActivityMapper.countCompletedByPlantAndType(userId, plantId, "water");
        long week = careActivityMapper.countCompletedByPlantBetweenDates(userId, plantId, weekStart, today);
        long wateringOutside = careActivityMapper.countWateringOutsideRecommendedTime(userId, plantId);
        List<WateringTimeSegmentCount> rawSegments = careActivityMapper.selectWateringTimeDistribution(userId, plantId);
        List<PlantStatusLogEntry> statusLogs = plantMapper.selectStatusLogsByPlant(userId, plantId);
        String latestDateText = careActivityMapper.selectLatestCompletedDateByPlant(userId, plantId);

        int lastGap = 0;
        try {
            if (latestDateText != null && !latestDateText.trim().isEmpty()) {
                LocalDate latestDate = LocalDate.parse(latestDateText);
                lastGap = (int) Math.max(0, ChronoUnit.DAYS.between(latestDate, today));
            }
        } catch (Exception ignore) {
            lastGap = 0;
        }

        PlantCareStats stats = new PlantCareStats();
        stats.setTotalCareCount(total);
        stats.setPhotoCount(photos);
        stats.setWaterCount(water);
        stats.setWeekCareCount(week);
        stats.setLastCareGap(lastGap);
        stats.setWateringOutsideRecommendedCount(wateringOutside);
        stats.setWateringTimeSuggested(wateringOutside <= 0);
        stats.setWateringTimeTip(wateringOutside > 0
                ? "检测到部分浇水不在早晚时段，建议尽量在早上或晚上浇水。"
                : "浇水时间分布良好，继续保持早晚浇水习惯。");
        stats.setWateringTimeDistribution(fillWateringSegments(rawSegments));
        stats.setStatusTimeDistribution(fillStatusTimeDistribution(plant, statusLogs));
        return stats;
    }

    public List<PlantMonthlyCount> listPlantMonthlyCounts(Long userId, Long plantId, Integer months) {
        int safeMonths = (months == null || months < 1) ? 6 : Math.min(months, 24);
        Plant plant = plantMapper.selectByUserIdAndId(userId, plantId);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + plantId);
        }

        LocalDate today = LocalDate.now();
        YearMonth endYm = YearMonth.from(today);
        YearMonth startYm = endYm.minusMonths(safeMonths - 1L);
        LocalDate startDate = startYm.atDay(1);
        LocalDate endDate = today;

        List<PlantMonthlyCount> dbRows = careActivityMapper.selectCompletedMonthlyCountsByPlant(userId, plantId, startDate, endDate);
        Map<String, Long> countMap = new HashMap<>();
        for (PlantMonthlyCount row : dbRows) {
            if (row == null || row.getMonth() == null) continue;
            countMap.put(row.getMonth(), row.getCareCount() == null ? 0L : row.getCareCount());
        }

        List<PlantMonthlyCount> result = new ArrayList<>();
        for (int i = 0; i < safeMonths; i++) {
            YearMonth ym = startYm.plusMonths(i);
            String key = ym.toString();
            PlantMonthlyCount item = new PlantMonthlyCount();
            item.setMonth(key);
            item.setCareCount(countMap.getOrDefault(key, 0L));
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void generateActivitiesForDate(Long userId, LocalDate targetDate) {
        List<CarePlanRuleEntity> dueRules = carePlanRuleMapper.selectDueRulesByUserAndDate(userId, targetDate);
        for (CarePlanRuleEntity rule : dueRules) {
            LocalDate due = rule.getNextDueDate();
            if (due == null || rule.getIntervalDays() == null || rule.getIntervalDays() <= 0) {
                continue;
            }
            while (!due.isAfter(targetDate)) {
                if (isSeasonMatched(rule.getSeason(), due)) {
                    CareActivityEntity exists = careActivityMapper.selectByRuleAndDate(userId, rule.getId(), due);
                    if (exists == null) {
                        CareActivityEntity insert = new CareActivityEntity();
                        insert.setUserId(rule.getUserId());
                        insert.setPlantId(resolvePlantIdByPlan(rule.getPlanId()));
                        insert.setPlanId(rule.getPlanId());
                        insert.setRuleId(rule.getId());
                        insert.setActivityType(rule.getActivityType());
                        insert.setScheduledDate(due);
                        careActivityMapper.insert(insert);
                    }
                }
                due = due.plusDays(rule.getIntervalDays());
            }
            carePlanRuleMapper.updateNextDueDate(rule.getId(), due);
        }
    }

    @Transactional
    public int generateActivitiesForAllUsers(LocalDate targetDate) {
        List<Long> userIds = carePlanMapper.selectEnabledUserIds();
        int touched = 0;
        for (Long userId : userIds) {
            generateActivitiesForDate(userId, targetDate);
            touched += 1;
        }
        return touched;
    }

    private Long resolvePlantIdByPlan(Long planId) {
        Long plantId = carePlanMapper.selectPlantIdByPlanId(planId);
        if (plantId == null) {
            throw new IllegalArgumentException("plan not found: " + planId);
        }
        return plantId;
    }

    private boolean isSeasonMatched(String season, LocalDate date) {
        if (season == null || "ALL".equalsIgnoreCase(season)) return true;
        return season.equalsIgnoreCase(careMetaService.seasonOf(date));
    }

    private String dayText(int offset) {
        if (offset <= 0) return "今天";
        if (offset == 1) return "明天";
        if (offset == 2) return "后天";
        return "第" + offset + "天";
    }

    private List<CareActivity> convertActivityRows(List<CareActivityEntity> rows) {
        List<CareActivity> result = new ArrayList<>();
        for (CareActivityEntity row : rows) {
            CareActivity item = new CareActivity();
            item.setId(String.valueOf(row.getId()));
            item.setDate(row.getScheduledDate().toString());
            item.setTime("09:00");
            item.setName(careMetaService.label(row.getActivityType()));
            item.setPlantName(row.getPlantName());
            item.setCompleted("COMPLETED".equalsIgnoreCase(row.getStatus()));
            item.setIcon(careMetaService.icon(row.getActivityType()));
            item.setRecord(parseRecord(row.getRecordJson()));
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> parseRecord(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private String buildRecordText(String activityType, Map<String, Object> record) {
        if (record == null || record.isEmpty()) {
            return "已完成本次养护记录";
        }

        String note = firstNonBlank(record, "note", "remark", "description", "desc", "content", "text");
        String type = activityType == null ? "" : activityType.trim().toLowerCase();
        String summary;
        if ("water".equals(type)) {
            String amount = valueAsText(record.get("amount"));
            String method = valueAsText(record.get("method"));
            summary = joinNonBlank("浇水" + (amount.isEmpty() ? "" : formatWithUnit(amount, "ml")), method);
        } else if ("fertilize".equals(type)) {
            String material = valueAsText(record.get("material"));
            summary = material.isEmpty() ? "完成施肥" : "施肥用料：" + material;
        } else if ("prune".equals(type)) {
            String part = valueAsText(record.get("part"));
            summary = part.isEmpty() ? "完成修剪" : "修剪部位：" + part;
        } else if ("repot".equals(type)) {
            String potSize = valueAsText(record.get("potSize"));
            summary = potSize.isEmpty() ? "完成换盆" : "更换花盆：" + potSize;
        } else if ("measure".equals(type)) {
            String weight = valueAsText(record.get("weight"));
            String height = valueAsText(record.get("height"));
            summary = joinNonBlank(
                    weight.isEmpty() ? "" : "重量" + weight + "g",
                    height.isEmpty() ? "" : "高度" + height + "cm"
            );
            if (summary.isEmpty()) summary = "完成测量";
        } else if ("pest".equals(type)) {
            String pestType = valueAsText(record.get("type"));
            String treatment = valueAsText(record.get("treatment"));
            summary = joinNonBlank(
                    pestType.isEmpty() ? "" : "问题类型：" + pestType,
                    treatment.isEmpty() ? "" : "处理方式：" + treatment
            );
            if (summary.isEmpty()) summary = "完成病虫害处理";
        } else if ("loosen".equals(type)) {
            summary = "完成松土";
        } else if ("photo".equals(type)) {
            summary = "已拍照记录";
        } else {
            summary = "已完成本次养护记录";
        }

        if (!note.isEmpty()) {
            return summary.isEmpty() ? note : summary + "，" + note;
        }
        if (!summary.isEmpty()) {
            return summary;
        }
        return "已完成本次养护记录";
    }

    private String firstNonBlank(Map<String, Object> record, String... keys) {
        for (String key : keys) {
            String value = valueAsText(record.get(key));
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String valueAsText(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).trim();
        if ("null".equalsIgnoreCase(text)) return "";
        return text;
    }

    private String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("，");
            sb.append(part.trim());
        }
        return sb.toString();
    }

    private String formatWithUnit(String raw, String unit) {
        if (raw == null) return "";
        String text = raw.trim();
        if (text.isEmpty()) return "";
        if (text.toLowerCase().endsWith(unit.toLowerCase())) {
            return text;
        }
        return text + unit;
    }

    private List<WateringTimeSegmentCount> fillWateringSegments(List<WateringTimeSegmentCount> rawSegments) {
        Map<String, Long> countMap = new HashMap<>();
        if (rawSegments != null) {
            for (WateringTimeSegmentCount item : rawSegments) {
                if (item == null || item.getSegment() == null) continue;
                countMap.put(item.getSegment(), item.getCount() == null ? 0L : item.getCount());
            }
        }
        List<WateringTimeSegmentCount> list = new ArrayList<>();
        list.add(createSegment("morning", countMap.getOrDefault("morning", 0L)));
        list.add(createSegment("daytime", countMap.getOrDefault("daytime", 0L)));
        list.add(createSegment("evening", countMap.getOrDefault("evening", 0L)));
        list.add(createSegment("night", countMap.getOrDefault("night", 0L)));
        return list;
    }

    private WateringTimeSegmentCount createSegment(String segment, Long count) {
        WateringTimeSegmentCount item = new WateringTimeSegmentCount();
        item.setSegment(segment);
        item.setCount(count == null ? 0L : count);
        return item;
    }

    private List<PlantStatusTimeDistributionItem> fillStatusTimeDistribution(Plant plant, List<PlantStatusLogEntry> logs) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = resolvePlantStartTime(plant);
        Map<String, Long> durationMap = new HashMap<>();
        durationMap.put("健康", 0L);
        durationMap.put("生病", 0L);
        durationMap.put("休眠", 0L);
        durationMap.put("已死亡", 0L);
        durationMap.put("已送人", 0L);
        durationMap.put("已售出", 0L);

        if (now.isBefore(startAt)) {
            startAt = now;
        }

        String currentStatus = normalizeStatusLabel(plant.getHealthStatus());
        String cursorStatus = currentStatus;

        List<PlantStatusLogEntry> sortedLogs = logs == null ? new ArrayList<>() : logs;
        PlantStatusLogEntry lastBefore = null;
        for (PlantStatusLogEntry entry : sortedLogs) {
            if (entry == null || entry.getChangedAt() == null) continue;
            if (!entry.getChangedAt().isAfter(startAt)) {
                lastBefore = entry;
            } else {
                break;
            }
        }
        if (lastBefore != null) {
            cursorStatus = normalizeStatusLabel(lastBefore.getStatus());
        } else if (!sortedLogs.isEmpty()) {
            cursorStatus = normalizeStatusLabel(sortedLogs.get(0).getStatus());
        }

        LocalDateTime cursorAt = startAt;
        for (PlantStatusLogEntry entry : sortedLogs) {
            if (entry == null || entry.getChangedAt() == null) continue;
            if (!entry.getChangedAt().isAfter(startAt)) continue;
            if (!entry.getChangedAt().isAfter(cursorAt)) {
                cursorStatus = normalizeStatusLabel(entry.getStatus());
                continue;
            }
            appendDuration(durationMap, cursorStatus, cursorAt, entry.getChangedAt());
            cursorAt = entry.getChangedAt();
            cursorStatus = normalizeStatusLabel(entry.getStatus());
        }
        appendDuration(durationMap, cursorStatus, cursorAt, now);

        List<PlantStatusTimeDistributionItem> result = new ArrayList<>();
        result.add(createStatusDurationItem("健康", durationMap.getOrDefault("健康", 0L)));
        result.add(createStatusDurationItem("生病", durationMap.getOrDefault("生病", 0L)));
        result.add(createStatusDurationItem("休眠", durationMap.getOrDefault("休眠", 0L)));
        result.add(createStatusDurationItem("已死亡", durationMap.getOrDefault("已死亡", 0L)));
        result.add(createStatusDurationItem("已送人", durationMap.getOrDefault("已送人", 0L)));
        result.add(createStatusDurationItem("已售出", durationMap.getOrDefault("已售出", 0L)));
        return result;
    }

    private PlantStatusTimeDistributionItem createStatusDurationItem(String status, Long durationMinutes) {
        PlantStatusTimeDistributionItem item = new PlantStatusTimeDistributionItem();
        item.setStatus(status);
        item.setDurationMinutes(durationMinutes == null ? 0L : durationMinutes);
        return item;
    }

    private void appendDuration(Map<String, Long> durationMap, String status, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || !to.isAfter(from)) return;
        long minutes = ChronoUnit.MINUTES.between(from, to);
        if (minutes <= 0) return;
        String key = normalizeStatusLabel(status);
        long current = durationMap.getOrDefault(key, 0L);
        durationMap.put(key, current + minutes);
    }

    private LocalDateTime resolvePlantStartTime(Plant plant) {
        if (plant == null) return LocalDateTime.now();
        try {
            if (plant.getPlantingDate() == null || plant.getPlantingDate().trim().isEmpty()) {
                return LocalDateTime.now();
            }
            return LocalDate.parse(plant.getPlantingDate()).atStartOfDay();
        } catch (Exception ignore) {
            return LocalDateTime.now();
        }
    }

    private String normalizeStatusLabel(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "健康";
        String value = raw.trim();
        if ("healthy".equalsIgnoreCase(value) || "健康".equals(value)) return "健康";
        if ("sick".equalsIgnoreCase(value) || "生病".equals(value)) return "生病";
        if ("dormant".equalsIgnoreCase(value) || "休眠".equals(value)) return "休眠";
        if ("dead".equalsIgnoreCase(value) || "死亡".equals(value) || "已死亡".equals(value)) return "已死亡";
        if ("gifted".equalsIgnoreCase(value) || "已送人".equals(value)) return "已送人";
        if ("sold".equalsIgnoreCase(value) || "已售出".equals(value)) return "已售出";
        return "健康";
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPhotoUrls(Map<String, Object> record) {
        List<String> photos = new ArrayList<>();
        if (record == null || record.isEmpty()) return photos;
        Object photoList = record.get("photos");
        if (photoList instanceof List) {
            for (Object value : (List<Object>) photoList) {
                String text = valueAsText(value);
                if (!text.isEmpty()) photos.add(text);
            }
        }
        String singlePhoto = valueAsText(record.get("photo"));
        if (!singlePhoto.isEmpty() && !photos.contains(singlePhoto)) {
            photos.add(singlePhoto);
        }
        return photos;
    }
}
