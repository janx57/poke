package com.janx57.poke;

import java.util.List;

import com.google.gerrit.common.errors.EmailException;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.server.mail.Address;
import com.google.gerrit.server.mail.EmailArguments;
import com.google.gerrit.server.mail.OutgoingEmail;
import com.google.gerrit.server.mail.RecipientType;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public final class PokeSender extends OutgoingEmail {
  private final List<Address> to;
  private final Change change;
  private final String issuer;

  public interface Factory {
    PokeSender create(List<Address> to, Change change, String issuer);
  }

  @Inject
  protected PokeSender(final EmailArguments ea, final String mc,
      @Assisted final List<Address> to, @Assisted final Change change,
      @Assisted final String issuer) {
    super(ea, mc);
    this.to = to;
    this.change = change;
    this.issuer = issuer;
  }

  @Override
  protected void init() throws EmailException {
    super.init();
    setHeader("Subject",
        String.format("[Gerrit Code Review] You've been poked by %s!", issuer));
    for (Address addr : to) {
      add(RecipientType.TO, addr);
    }
  }

  @Override
  protected void format() throws EmailException {
    appendText(velocifyFile("Poke.vm"));
  }

  public String getChangeUrl() {
    if (getGerritUrl() != null) {
      final StringBuilder r = new StringBuilder();
      r.append(getGerritUrl());
      r.append(change.getChangeId());
      return r.toString();
    }
    return null;
  }
}
