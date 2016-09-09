package org.beigesoft.ajetty;

/*
 * Beigesoft ™
 *
 * Licensed under the Apache License, Version 2.0
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.deploy.PropertiesConfigurationManager;

import org.beigesoft.afactory.IFactoryAppBeans;

/**
 * <p>
 * BootStrap for Jetty for Android configured as minimum server
 * with WebAppDeployer that deploy ordinal non-JSP web-app
 * (JSP/JSTL must be precompiled into servlets) that must be unpacked
 * into [jetty-base]/webapps.
 * It must be follow working directory(jetty:base) containing:
 * <pre>
 * webdefault.xml
 * webapps
 * </pre>
 * </p>
 *
 * @author Yury Demidenko
 */
public class BootStrap {

  /**
   * <p>Factory app-beans.</p>
   **/
  private IFactoryAppBeans factoryAppBeans;

  /**
   * <p>Port.</p>
   **/
  private int port = 8080;

  /**
   * <p>Jetty base.</p>
   **/
  private String jettyBase = "";

  /**
   * <p>Jetty.</p>
   **/
  private Server server;

  /**
   * <p>Is started.</p>
   **/
  private boolean isStarted = false;

  /**
   * <p>Deployment Manager.</p>
   **/
  private DeploymentManager deploymentManager;

  /**
   * <p>Create and configure server.</p>
   * @throws Exception an Exception
   **/
  public final void createServer() throws Exception {
    // Create a basic jetty server object that will listen on port 8080.
    // Note that if you set this to port 0 then a randomly available port
    // will be assigned that you can either look in the logs for the port,
    // or programmatically obtain it for use in test cases.
    this.server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(this.port);
    connector.setHost("127.0.0.1");
    server.setConnectors(new Connector[]{connector});
    // Handlers:
    HandlerCollection handlers = new HandlerCollection();
    DefaultHandler defaultHandler = new DefaultHandler();
    ContextHandlerCollection contextHandlerCollection =
      new ContextHandlerCollection();
    handlers.setHandlers(new Handler[] {contextHandlerCollection,
      defaultHandler }); // !!! defaultHandler must be second
    this.server.setHandler(handlers);
    // Create the deployment manager:
    deploymentManager = new DeploymentManager();
    deploymentManager.setContexts(contextHandlerCollection);
    WebAppProvider webAppProvider = new WebAppProvider();
    webAppProvider.setFactoryAppBeans(this.factoryAppBeans);
    webAppProvider.setMonitoredDirName(jettyBase + File.separator + "webapps");
    webAppProvider.setDefaultsDescriptor(jettyBase + File.separator
      + "webdefault.xml");
    webAppProvider.setExtractWars(false);
    PropertiesConfigurationManager confManager =
      new PropertiesConfigurationManager();
    webAppProvider.setConfigurationManager(confManager);
    deploymentManager.addAppProvider(webAppProvider);
    this.server.addBean(deploymentManager);
  }


  /**
   * <p>Start server.</p>
   * @throws Exception an Exception
   **/
  public final void startServer() throws Exception {
    this.server.start();
    this.isStarted = true;
  }

  /**
   * <p>Stop server.</p>
   * @throws Exception an Exception
   **/
  public final void stopServer() throws Exception {
    this.server.stop();
    this.isStarted = false;
  }

  /**
   * <p>This start preconfigured Jetty on non-Android OS.
   * It may takes up to tho parameters: port and jetty:base.
   * Example:
   * <pre>
   * java -jar a-jetty-base.jar jetty:base=/home/my/a-jetty
   * or
   * java -jar a-jetty-base.jar jetty:base=/home/my/a-jetty port=8080
   * </pre>
   * </p>
   * @param pArgs arguments
   **/
  public static final void main(final String[] pArgs) {
    try {
      BootStrap bootStrap = new BootStrap();
      for (String arg : pArgs) {
        if (arg.contains("port=")) {
          String strPort = arg.replace("port=", "").trim();
          bootStrap.setPort(Integer.parseInt(strPort));
        } else if (arg.contains("jetty:base=")) {
          bootStrap.setJettyBase(arg.replace("jetty:base=", "").trim());
        }
      }
      bootStrap.setFactoryAppBeans(new FactoryAppBeansStd());
      bootStrap.createServer();
      bootStrap.startServer();
      // The use of server.join() the will make the current thread join and
      // wait until the server is done executing.
      // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join
      bootStrap.getServer().join();
      bootStrap.setIsStarted(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Simple getters and setters:
  /**
   * <p>Getter for port.</p>
   * @return int
   **/
  public final int getPort() {
    return this.port;
  }

  /**
   * <p>Setter for port.</p>
   * @param pPort reference
   **/
  public final void setPort(final int pPort) {
    this.port = pPort;
  }

  /**
   * <p>Getter for jettyBase.</p>
   * @return String
   **/
  public final String getJettyBase() {
    return this.jettyBase;
  }

  /**
   * <p>Setter for jettyBase.</p>
   * @param pJettyBase reference
   **/
  public final void setJettyBase(final String pJettyBase) {
    this.jettyBase = pJettyBase;
  }

  /**
   * <p>Getter for server.</p>
   * @return Server
   **/
  public final Server getServer() {
    return this.server;
  }

  /**
   * <p>Setter for server.</p>
   * @param pServer reference
   **/
  public final void setServer(final Server pServer) {
    this.server = pServer;
  }

  /**
   * <p>Getter for isStarted.</p>
   * @return boolean
   **/
  public final boolean getIsStarted() {
    return this.isStarted;
  }

  /**
   * <p>Setter for isStarted.</p>
   * @param pIsStarted reference
   **/
  public final void setIsStarted(final boolean pIsStarted) {
    this.isStarted = pIsStarted;
  }

  /**
   * <p>Getter for factoryAppBeans.</p>
   * @return IFactoryAppBeans
   **/
  public final IFactoryAppBeans getFactoryAppBeans() {
    return this.factoryAppBeans;
  }

  /**
   * <p>Setter for factoryAppBeans.</p>
   * @param pFactoryAppBeans reference
   **/
  public final void setFactoryAppBeans(
    final IFactoryAppBeans pFactoryAppBeans) {
    this.factoryAppBeans = pFactoryAppBeans;
  }

  /**
   * <p>Getter for deploymentManager.</p>
   * @return DeploymentManager
   **/
  public final DeploymentManager getDeploymentManager() {
    return this.deploymentManager;
  }

  /**
   * <p>Setter for deploymentManager.</p>
   * @param pDeploymentManager reference
   **/
  public final void setDeploymentManager(
    final DeploymentManager pDeploymentManager) {
    this.deploymentManager = pDeploymentManager;
  }
}
