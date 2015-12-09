package net.selenate.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class C {
  public final String ServerHost;
  public final String ClientHost;
  public final String ServerPort;
  public final String ServerPath;

  // === CONSTRUCTORS ===

  /**
   * CREATES CONFIGURATION FROM DEFAULT DISK LOCATION OR SYSTEM PROPERTIES
   */
  public C() {
    try {
      final String configPath = System.getProperty("user.home") + "/.config/selenate/" + System.getProperty("branch") + "/client.config";
      final Properties config = new Properties();
      final Properties props  = System.getProperties();

      try {
        final FileInputStream pis = new FileInputStream(configPath);
        try {
          config.load(pis);
        } finally {
          pis.close();
        }
      } catch (final Exception e) {
        throw new IOException(String.format("An error occured while trying to read the config file at [%s]!", configPath), e);
      }

      final String configServerHost = getNonNullProperty(config, "server.host");
      final String configClientHost = getNonNullProperty(config, "client.host");
      final String configServerPort = getNonNullProperty(config, "server.port");
      final String configServerPath = getNonNullProperty(config, "server.path");

      final String propsServerHost  = props.getProperty("server.host");
      final String propsClientHost  = props.getProperty("client.host");
      final String propsServerPort  = props.getProperty("server.port");
      final String propsServerPath  = props.getProperty("server.path");

      this.ServerHost = orElse(propsServerHost, configServerHost);
      this.ClientHost = orElse(propsClientHost, configClientHost);
      this.ServerPort = orElse(propsServerPort, configServerPort);
      this.ServerPath = orElse(propsServerPath, configServerPath);
    } catch (final Exception e) {
      throw new RuntimeException("An error occured while loading configuration!", e);
    }
  }

  /**
   * CREATES CONFIGURATION FROM PROVIDED PROPERTIES
   * @param properties
   */
  public C(final Properties properties) {
    this.ClientHost = getNonNullProperty(properties, "client.host");
    this.ServerHost = getNonNullProperty(properties, "server.host");
    this.ServerPort = getNonNullProperty(properties, "server.port");
    this.ServerPath = getNonNullProperty(properties, "server.path");
  }

  /**
   * CREATES CUSTOM CONFIGURATION
   * @param clientHost
   * @param serverHost
   * @param serverPort
   * @param serverPath
   */
  public C(final String clientHost, final String serverHost, final String serverPort, final String serverPath) {
    this.ClientHost = clientHost;
    this.ServerHost = serverHost;
    this.ServerPort = serverPort;
    this.ServerPath = serverPath;
  }

  // ---------------------------------------------------------------------------

  private static String getNonNullProperty(final Properties props, final String propName) {
    final String prop = props.getProperty(propName);
    if (prop == null) {
      throw new IllegalArgumentException(String.format("\"%s\" property in the config file must be defined!", propName));
    } else {
      return prop;
    }
  }

  private static String orElse(final String s1, final String s2) {
    if (s1 != null) return s1;
    if (s2 != null) {
      return s2;
    } else {
      throw new NullPointerException("Second argument to orElse cannot be null!");
    }
  }
}