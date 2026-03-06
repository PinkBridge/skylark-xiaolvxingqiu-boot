package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.model.CareActivity;
import cn.skylark.xiaolvxingqiu.boot.model.CareTask;
import cn.skylark.xiaolvxingqiu.boot.model.Feedback;
import cn.skylark.xiaolvxingqiu.boot.model.GardenInfo;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AppDataService {

    private final AtomicLong plantIdGenerator = new AtomicLong(100);
    private final AtomicLong feedbackIdGenerator = new AtomicLong(1);

    private GardenInfo gardenInfo;
    private UserProfile userProfile;
    private final Map<Long, Plant> plantMap = new LinkedHashMap<>();
    private final Map<String, CareTask> careTaskMap = new LinkedHashMap<>();
    private final List<CareActivity> careActivities = new ArrayList<>();
    private final List<Feedback> feedbackList = new ArrayList<>();

    @PostConstruct
    public void init() {
        gardenInfo = new GardenInfo();
        gardenInfo.setTitle("我的莫奈花园");
        gardenInfo.setSubTitle("2020-05-15");
        gardenInfo.setThumb("https://img11.360buyimg.com/n7/jfs/t1/94448/29/2734/524808/5dd4cc16E990dfb6b/59c256f85a8c3757.jpg");
        gardenInfo.setImage("https://img11.360buyimg.com/n7/jfs/t1/94448/29/2734/524808/5dd4cc16E990dfb6b/59c256f85a8c3757.jpg");
        gardenInfo.setDescription("欢迎来到小绿星球");

        userProfile = new UserProfile();
        userProfile.setAvatar("https://cdn.uviewui.com/uview/example/button.png");
        userProfile.setName("姚 棉伟");
        userProfile.setGender("male");
        userProfile.setMotto("每一份生命都值得尊重和呵护!");

        addPlant(seedPlant(1L, "常春藤", "healthy", "健康", 28, true, "/static/flower/857f855fcea81e07d1c315589d5d5a30.jpg"));
        addPlant(seedPlant(2L, "龟背竹", "sick", "生病", 13, false, "/static/flower/b22cd3cf854015e988176e17dab8a554.jpg"));
        addPlant(seedPlant(3L, "绿萝", "healthy", "健康", 17, true, "/static/flower/4c43268b47b31cfab3d434d474ad728c.jpg"));

        addTask(seedTask("t1", "浇水", "常春藤", 0, "今天 09:30", false, "/static/icon/water.png"));
        addTask(seedTask("t2", "施肥", "龟背竹", 0, "今天 10:00", false, "/static/icon/fertilize.png"));
        addTask(seedTask("t3", "测量", "绿萝", 1, "明天 20:00", false, "/static/icon/measure.png"));

        CareActivity activity = new CareActivity();
        activity.setId("a1");
        activity.setDate(LocalDate.now().minusDays(1).toString());
        activity.setTime("09:20");
        activity.setName("浇水");
        activity.setPlantName("常春藤");
        activity.setCompleted(true);
        activity.setIcon("/static/icon/water.png");
        activity.setRecord(new LinkedHashMap<String, Object>());
        activity.getRecord().put("amount", "180");
        activity.getRecord().put("method", "喷雾");
        activity.getRecord().put("note", "表土偏干，补水后叶片恢复挺立");
        careActivities.add(activity);
    }

    public GardenInfo getGardenInfo() {
        return gardenInfo;
    }

    public void updateGardenInfo(GardenInfo newGardenInfo) {
        this.gardenInfo = newGardenInfo;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void updateUserProfile(UserProfile profile) {
        this.userProfile = profile;
    }

    public List<Plant> listPlants(String filter) {
        List<Plant> plants = new ArrayList<>(plantMap.values());
        if (filter == null || filter.trim().isEmpty() || "all".equalsIgnoreCase(filter)) {
            return plants;
        }
        if ("healthy".equalsIgnoreCase(filter)) {
            return plants.stream().filter(p -> "healthy".equalsIgnoreCase(p.getHealthStatus())).collect(Collectors.toList());
        }
        if ("abnormal".equalsIgnoreCase(filter)) {
            return plants.stream().filter(p -> !"healthy".equalsIgnoreCase(p.getHealthStatus())).collect(Collectors.toList());
        }
        if ("todo".equalsIgnoreCase(filter)) {
            return plants.stream().filter(p -> p.getTodayCareTasks() != null && !p.getTodayCareTasks().isEmpty()).collect(Collectors.toList());
        }
        if ("favorite".equalsIgnoreCase(filter)) {
            return plants.stream().filter(p -> Boolean.TRUE.equals(p.getFavorite())).collect(Collectors.toList());
        }
        return plants;
    }

    public Plant getPlant(Long id) {
        Plant plant = plantMap.get(id);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        return plant;
    }

    public Plant createPlant(Plant plant) {
        plant.setId(plantIdGenerator.incrementAndGet());
        if (plant.getFavorite() == null) {
            plant.setFavorite(false);
        }
        if (plant.getHealthStatus() == null) {
            plant.setHealthStatus("healthy");
        }
        if (plant.getStatusLabel() == null) {
            plant.setStatusLabel("健康");
        }
        if (plant.getDays() == null) {
            plant.setDays(1);
        }
        if (plant.getTodayCareTasks() == null) {
            plant.setTodayCareTasks(new ArrayList<String>());
        }
        plantMap.put(plant.getId(), plant);
        return plant;
    }

    public Plant updatePlant(Long id, Plant request) {
        Plant old = getPlant(id);
        request.setId(old.getId());
        if (request.getFavorite() == null) {
            request.setFavorite(old.getFavorite());
        }
        if (request.getTodayCareTasks() == null) {
            request.setTodayCareTasks(old.getTodayCareTasks());
        }
        if (request.getDays() == null) {
            request.setDays(old.getDays());
        }
        if (request.getHealthStatus() == null) {
            request.setHealthStatus(old.getHealthStatus());
        }
        if (request.getStatusLabel() == null) {
            request.setStatusLabel(old.getStatusLabel());
        }
        plantMap.put(id, request);
        return request;
    }

    public void deletePlant(Long id) {
        if (plantMap.remove(id) == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
    }

    public List<CareTask> listCareTasks() {
        return new ArrayList<>(careTaskMap.values());
    }

    public CareTask completeTask(String taskId) {
        CareTask task = careTaskMap.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found: " + taskId);
        }
        task.setCompleted(true);
        task.setTimeText("今天 " + LocalDateTime.now().getHour() + ":" + String.format("%02d", LocalDateTime.now().getMinute()));
        return task;
    }

    public List<CareActivity> listActivitiesByDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return new ArrayList<>(careActivities);
        }
        return careActivities.stream().filter(item -> date.equals(item.getDate())).collect(Collectors.toList());
    }

    public List<CareActivity> listActivitiesByMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return new ArrayList<>(careActivities);
        }
        return careActivities.stream().filter(item -> item.getDate() != null && item.getDate().startsWith(month)).collect(Collectors.toList());
    }

    public Feedback saveFeedback(Feedback feedback) {
        feedback.setId(feedbackIdGenerator.getAndIncrement());
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackList.add(feedback);
        return feedback;
    }

    public List<Feedback> listFeedback() {
        return feedbackList;
    }

    private Plant seedPlant(Long id, String name, String healthStatus, String statusLabel, Integer days, boolean favorite, String image) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        plant.setHealthStatus(healthStatus);
        plant.setStatusLabel(statusLabel);
        plant.setDays(days);
        plant.setFavorite(favorite);
        plant.setImage(image);
        plant.setCultivationType("soil");
        plant.setPlantingDate(LocalDate.now().minusDays(days).toString());
        plant.setTodayCareTasks(new ArrayList<String>());
        if (id == 1L) {
            plant.getTodayCareTasks().add("浇水");
        }
        return plant;
    }

    private CareTask seedTask(String id, String name, String plantName, int offset, String timeText, boolean completed, String icon) {
        CareTask task = new CareTask();
        task.setId(id);
        task.setName(name);
        task.setPlantName(plantName);
        task.setOffset(offset);
        task.setTimeText(timeText);
        task.setCompleted(completed);
        task.setIcon(icon);
        return task;
    }

    private void addPlant(Plant plant) {
        plantMap.put(plant.getId(), plant);
    }

    private void addTask(CareTask task) {
        careTaskMap.put(task.getId(), task);
    }
}
