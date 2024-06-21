/*
 * Author spacenough 2024.
 */

package pl.codelen.edutor.bot.yaml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YamlBase {
  private Map<String, Object> data;
  private final File document;
  private final InputStream defaults;

  public YamlBase(File document, InputStream defaults) {
    this.document = document;
    this.defaults = defaults;
    loadYaml();
    updateConfig();
  }

  private void loadYaml() {
    try {
      if (!document.getParentFile().exists()) {
        document.getParentFile().mkdirs();
      }

      if (!document.exists()) {
        document.createNewFile();
        if (defaults != null) {
          Files.copy(defaults, document.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      }

      try (FileInputStream fileInputStream = new FileInputStream(document)) {
        LoaderOptions loadOptions = new LoaderOptions();
        DumperOptions dumpOptions = new DumperOptions();

        loadOptions.setProcessComments(true);
        dumpOptions.setIndent(2);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOptions.setPrettyFlow(true);
        dumpOptions.setProcessComments(true);

        Yaml yaml = new Yaml(new SafeConstructor(loadOptions), new Representer(dumpOptions), dumpOptions, loadOptions);
        this.data = yaml.load(fileInputStream);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Nullable
  private Map<String, Object> loadDefaultConfig() {
    try (InputStream resourceStream = defaults) {
      if (resourceStream == null) {
        throw new FileNotFoundException("yaml file not found in resources");
      }

      LoaderOptions loadOptions = new LoaderOptions();
      DumperOptions dumpOptions = new DumperOptions();

      loadOptions.setProcessComments(true);
      dumpOptions.setIndent(2);
      dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      dumpOptions.setPrettyFlow(true);
      dumpOptions.setProcessComments(true);

      Yaml yaml = new Yaml(new SafeConstructor(loadOptions), new Representer(dumpOptions), dumpOptions, loadOptions);
      return yaml.load(resourceStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void mergeConfigs(@NotNull Map<String, Object> defaultConfig, Map<String, Object> localConfig) {
    for (String key : defaultConfig.keySet()) {
      if (!localConfig.containsKey(key)) {
        localConfig.put(key, defaultConfig.get(key));
      } else if (defaultConfig.get(key) instanceof Map && localConfig.get(key) instanceof Map) {
        mergeConfigs((Map<String, Object>) defaultConfig.get(key), (Map<String, Object>) localConfig.get(key));
      }
    }
  }

  public void reload() {
    loadYaml();
    updateConfig();
  }

  private void updateConfig() {
    Map<String, Object> defaultConfig = loadDefaultConfig();
    if (defaultConfig != null) {
      int defaultVersion = Integer.parseInt(defaultConfig.getOrDefault("config-version", "1").toString());
      int localVersion = Integer.parseInt(Objects.requireNonNull(getValue("config-version", "1")).toString());

      if (localVersion < defaultVersion) {
        mergeConfigs(defaultConfig, this.data);
        this.data.put("config-version", defaultVersion);
        saveYaml();
      }
    }
  }

  private void saveYaml() {
    try (FileOutputStream fileOutputStream = new FileOutputStream(document)) {
      DumperOptions dumpOptions = new DumperOptions();
      dumpOptions.setIndent(2);
      dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      dumpOptions.setPrettyFlow(true);
      dumpOptions.setProcessComments(true);

      Yaml yaml = new Yaml(new Representer(dumpOptions), dumpOptions);
      yaml.dump(data, new OutputStreamWriter(fileOutputStream));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Object getValue(@NotNull String key, Object defaultValue) {
    String[] keys = key.split("\\.");
    Map<String, Object> currentMap = data;
    Object value = null;

    for (int i = 0; i < keys.length; i++) {
      value = currentMap.get(keys[i]);
      if (i < keys.length - 1) {
        if (value instanceof Map) {
          currentMap = (Map<String, Object>) value;
        } else {
          return defaultValue;
        }
      }
    }

    return value != null ? value : defaultValue;
  }

  public String getString(String key) {
    Object value = getValue(key, "");
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  public Integer getInteger(String key) {
    Object value = getValue(key, 0);
    if (value instanceof Integer) {
      return (Integer) value;
    }
    return 0;
  }

  public Float getFloat(String key) {
    Object value = getValue(key, 0f);
    if (value instanceof Float) {
      return (Float) value;
    } else if (value instanceof Double) {
      return ((Double) value).floatValue();
    }
    return 0f;
  }

  public Double getDouble(String key) {
    Object value = getValue(key, 0d);
    if (value instanceof Double) {
      return (Double) value;
    }
    return 0d;
  }

  public Boolean getBoolean(String key) {
    Object value = getValue(key, false);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getList(String key, Class<T> clazz) {
    Object value = getValue(key, null);
    if (value instanceof List<?> list) {
      for (Object item : list) {
        if (!clazz.isInstance(item)) {
          return null;
        }
      }
      return (List<T>) list;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <V> Map<String, V> getMap(String key, Class<V> clazz) {
    Object value = getValue(key, null);
    if (value instanceof Map<?, ?> map) {
      for (Object item : map.values()) {
        if (!clazz.isInstance(item)) {
          return null;
        }
      }
      return (Map<String, V>) map;
    }
    return null;
  }
}
