package com.github.tvbox.osc.cache;

import android.util.Log;

import com.github.tvbox.osc.data.AppDataManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 类描述:
 *
 * @author pj567
 * @since 2020/5/15
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();
    //反序列,把二进制数据转换成java object对象
    private static Object toObject(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (Exception e) {
            Log.e(TAG, "toObject error", e);
            return null;
        }
    }

    private static <T> byte[] toByteArray(T body) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(body);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "toByteArray error", e);
            return null;
        }
    }

    public static <T> void delete(String key, T body) {
        byte[] data = toByteArray(body);
        if (data == null) return;
        Cache cache = new Cache();
        cache.key = key;
        cache.data = data;
        AppDataManager.get().getCacheDao().delete(cache);
    }

    public static <T> void save(String key, T body) {
        byte[] data = toByteArray(body);
        if (data == null) return;
        Cache cache = new Cache();
        cache.key = key;
        cache.data = data;
        AppDataManager.get().getCacheDao().save(cache);
    }

    public static Object getCache(String key) {
        Cache cache = AppDataManager.get().getCacheDao().getCache(key);
        if (cache != null && cache.data != null) {
            return toObject(cache.data);
        }
        return null;
    }
}
