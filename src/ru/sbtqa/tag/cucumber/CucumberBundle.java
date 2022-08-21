package ru.sbtqa.tag.cucumber;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class CucumberBundle extends ResourceBundle{

  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return AbstractBundle.message(getBundle(), key, params);
  }

  @NonNls public static final String BUNDLE = "org.jetbrains.plugins.cucumber.CucumberBundle";
  private static Reference<ResourceBundle> ourBundle;

  @Override
  protected Object handleGetObject(@NotNull String key) {
    return getBundle().getObject(key);
  }

  @NotNull
  @Override
  public Enumeration<String> getKeys() {
    return getBundle().getKeys();
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
