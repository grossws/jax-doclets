package com.lunatech.doclets.jax.jaxrs;

import com.lunatech.doclets.jax.JAXConfiguration;
import com.lunatech.doclets.jax.Utils;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;

public class JAXRSConfiguration extends JAXConfiguration {

  public String jaxrscontext;
  
  public boolean enableHTTPExample;

  public boolean enableJavaScriptExample;

  public JAXRSConfiguration(ConfigurationImpl conf) {
    super(conf);
  }

  public void setOptions() {
    super.setOptions();
    String[][] options = parentConfiguration.root.options();
    jaxrscontext = Utils.getOption(options, "-jaxrscontext");
    enableHTTPExample = !Utils.hasOption(options, "-disablehttpexample");
    enableJavaScriptExample = !Utils.hasOption(options, "-disablejavascriptexample");
  }
}
