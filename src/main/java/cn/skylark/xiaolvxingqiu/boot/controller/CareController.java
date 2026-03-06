package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.CareActivity;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanConfigRequest;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanConfigResponse;
import cn.skylark.xiaolvxingqiu.boot.model.CareTask;
import cn.skylark.xiaolvxingqiu.boot.model.PlantAlbumPage;
import cn.skylark.xiaolvxingqiu.boot.model.PlantCareStats;
import cn.skylark.xiaolvxingqiu.boot.model.PlantGrowthRecordPage;
import cn.skylark.xiaolvxingqiu.boot.model.PlantMonthlyCount;
import cn.skylark.xiaolvxingqiu.boot.service.CareActivityService;
import cn.skylark.xiaolvxingqiu.boot.service.CarePlanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/care")
public class CareController {

    private final CarePlanService carePlanService;
    private final CareActivityService careActivityService;
    private final UserContextProvider userContextProvider;

    public CareController(CarePlanService carePlanService,
                          CareActivityService careActivityService,
                          UserContextProvider userContextProvider) {
        this.carePlanService = carePlanService;
        this.careActivityService = careActivityService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping("/plans/{plantId}")
    public ApiResponse<CarePlanConfigResponse> getPlan(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                        @PathVariable("plantId") Long plantId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(carePlanService.getPlanConfig(userId, plantId));
    }

    @PutMapping("/plans/{plantId}")
    public ApiResponse<CarePlanConfigResponse> savePlan(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                         @PathVariable("plantId") Long plantId,
                                                         @Validated @RequestBody CarePlanConfigRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(carePlanService.savePlanConfig(userId, plantId, request));
    }

    @PostMapping("/plans/generate")
    public ApiResponse<Map<String, Object>> generate(@RequestParam(value = "date", required = false) String date) {
        LocalDate targetDate = (date == null || date.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        int users = careActivityService.generateActivitiesForAllUsers(targetDate);
        return ApiResponse.success(new java.util.LinkedHashMap<String, Object>() {{
            put("date", targetDate.toString());
            put("touchedUsers", users);
        }});
    }

    @GetMapping("/tasks")
    public ApiResponse<List<CareTask>> listTasks(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                  @RequestParam(value = "gardenId", required = false) Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.listTasks(userId, gardenId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<CareTask> completeTask(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                              @PathVariable("taskId") String taskId,
                                              @RequestBody(required = false) Map<String, Object> body) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.completeTask(userId, taskId, body));
    }

    @GetMapping("/activities")
    public ApiResponse<List<CareActivity>> listActivities(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                          @RequestParam(value = "date", required = false) String date,
                                                          @RequestParam(value = "month", required = false) String month,
                                                          @RequestParam(value = "gardenId", required = false) Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        if (date != null && !date.trim().isEmpty()) {
            return ApiResponse.success(careActivityService.listActivitiesByDate(userId, date, gardenId));
        }
        if (month != null && !month.trim().isEmpty()) {
            return ApiResponse.success(careActivityService.listActivitiesByMonth(userId, month, gardenId));
        }
        return ApiResponse.success(careActivityService.listActivitiesByDate(userId, LocalDate.now().toString(), gardenId));
    }

    @GetMapping("/records")
    public ApiResponse<PlantGrowthRecordPage> listPlantGrowthRecords(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                                      @RequestParam("plantId") Long plantId,
                                                                      @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.listPlantGrowthRecords(userId, plantId, pageNo, pageSize));
    }

    @GetMapping("/albums")
    public ApiResponse<PlantAlbumPage> listPlantAlbumRecords(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                              @RequestParam("plantId") Long plantId,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.listPlantAlbumRecords(userId, plantId, pageNo, pageSize));
    }

    @GetMapping("/stats")
    public ApiResponse<PlantCareStats> getPlantCareStats(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                          @RequestParam("plantId") Long plantId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.getPlantCareStats(userId, plantId));
    }

    @GetMapping("/stats/monthly")
    public ApiResponse<List<PlantMonthlyCount>> getPlantMonthlyStats(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                                     @RequestParam("plantId") Long plantId,
                                                                     @RequestParam(value = "months", required = false) Integer months) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(careActivityService.listPlantMonthlyCounts(userId, plantId, months));
    }
}
