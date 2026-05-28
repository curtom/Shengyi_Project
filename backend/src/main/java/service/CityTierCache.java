package service;

import entity.CityTierEntity;
import mapper.CityTierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class CityTierCache {
    private final CityTierMapper cityTierMapper;
    private final Map<String, String> cityNoToTier = new ConcurrentHashMap<>();
    private final Map<String, String> cityNameToTier = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    private void ensureLoaded() {
        if (loaded.compareAndSet(false, true)) {
            try {
                List<CityTierEntity> list = cityTierMapper.selectList(null);
                for (CityTierEntity c : list) {
                    cityNoToTier.put(c.getCityNo(), c.getTier());
                    cityNameToTier.put(c.getCityName(), c.getTier());
                }
            } catch (Exception e) {
                loaded.set(false);
                throw e;
            }
        }
    }

    public BigDecimal mealStandard(String cityNo, String cityName) {
        ensureLoaded();
        String tier = cityNo != null ? cityNoToTier.get(cityNo) : null;
        if (tier == null && cityName != null) {
            tier = cityNameToTier.get(cityName);
        }
        if (tier == null) {
            tier = "3";
        }
        if ("1".equals(tier)) {
            return BigDecimal.valueOf(100);
        }
        if ("2".equals(tier)) {
            return BigDecimal.valueOf(80);
        }
        return BigDecimal.valueOf(50);
    }

    public Map<String, Object> allCities() {
        ensureLoaded();
        Map<String, Object> result = new HashMap<>();
        result.put("cities", cityTierMapper.selectList(null));
        return result;
    }
}
