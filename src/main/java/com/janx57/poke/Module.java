package com.janx57.poke;

import static com.google.gerrit.server.change.RevisionResource.REVISION_KIND;
import static com.google.inject.Scopes.SINGLETON;

import com.google.gerrit.extensions.restapi.RestApiModule;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.config.AllProjectsNameProvider;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class Module extends AbstractModule {

  @Override
  protected void configure() {
    bind(AllProjectsName.class).toProvider(AllProjectsNameProvider.class).in(
        SINGLETON);
    install(new FactoryModuleBuilder().build(PokeSender.Factory.class));
    install(new RestApiModule() {
      @Override
      protected void configure() {
        post(REVISION_KIND, "poke").to(PokeAction.class);
      }
    });
  }
}
