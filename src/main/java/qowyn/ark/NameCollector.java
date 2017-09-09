package qowyn.ark;

import qowyn.ark.types.ArkName;

@FunctionalInterface
public interface NameCollector {

  public void accept(ArkName name);

}
