package com.janx57.poke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gerrit.common.errors.EmailException;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.extensions.webui.UiAction;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.PatchSetApproval;
import com.google.gerrit.reviewdb.client.PatchSetApproval.LabelId;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.mail.Address;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;

final class PokeAction implements UiAction<RevisionResource>,
    RestModifyView<RevisionResource, PokeAction.Input> {
  static class Input {
  }

  private static final LabelId codeReview = new LabelId("Code-Review");

  private final Provider<CurrentUser> user;
  private final Provider<ReviewDb> db;
  private final PokeSender.Factory pokeSenderFactory;
  private final ChangeData.Factory changeDataFactory;

  @Inject
  PokeAction(final Provider<CurrentUser> user, final Provider<ReviewDb> db,
      final PokeSender.Factory pokeSenderFactory,
      final ChangeData.Factory changeDataFactory) {
    this.user = user;
    this.db = db;
    this.pokeSenderFactory = pokeSenderFactory;
    this.changeDataFactory = changeDataFactory;
  }

  @Override
  public String apply(final RevisionResource rev, final Input input)
      throws OrmException, EmailException {
    ChangeData cd = changeDataFactory.create(db.get(), rev.getChange());

    Collection<PatchSetApproval> approvals = cd.approvals().values();
    Set<Account.Id> remainingReviewers =
        getRemainingReviewers(approvals, cd.currentPatchSet().getId());
    remainingReviewers.remove(getCurrentUser().getId());

    if (remainingReviewers.size() == 0) {
      return "Looks like there's no one to poke!";
    }
    StringBuilder summaryMsg = new StringBuilder();
    summaryMsg.append("A poke e-mail has been sent to following users: \n\n");
    List<Address> recipients = new ArrayList<>(remainingReviewers.size());
    for (Account.Id id : remainingReviewers) {
      Address addr = getAddressBy(id);
      recipients.add(addr);
      summaryMsg.append(String.format("%s (%s)\n", addr.getName(),
          addr.getEmail()));
    }

    PokeSender mail =
        pokeSenderFactory.create(recipients, rev.getChange(), getCurrentUser()
            .getFullName());
    mail.send();
    return summaryMsg.toString();
  }

  @Override
  public Description getDescription(final RevisionResource resource) {
    ChangeData cd = changeDataFactory.create(db.get(), resource.getChange());

    Account.Id owner = null;
    Account.Id uploader = null;
    try {
      owner = cd.change().getOwner();
      uploader = cd.currentPatchSet().getUploader();
    } catch (OrmException e) {
      throw new RuntimeException(e);
    }
    Account.Id curr = getCurrentUser().getId();
    boolean visible =
        curr != null && (curr.equals(owner) || curr.equals(uploader));

    return new Description().setVisible(visible).setLabel("Poke")
        .setTitle("Poke reviewers who haven't voted yet");
  }

  private Set<Account.Id> getRemainingReviewers(
      final Collection<PatchSetApproval> approvals,
      final PatchSet.Id currPatchset) {
    Set<Account.Id> remainingReviewers = new HashSet<>();
    for (PatchSetApproval psa : approvals) {
      remainingReviewers.add(psa.getAccountId());
    }
    for (PatchSetApproval psa : approvals) {
      if (psa.getLabelId().equals(codeReview) && psa.getValue() != 0
          && psa.getPatchSetId().equals(currPatchset)) {
        remainingReviewers.remove(psa.getAccountId());
      }
    }

    return remainingReviewers;
  }

  private Address getAddressBy(final Account.Id id) throws OrmException {
    Account account = db.get().accounts().get(id);
    return new Address(account.getFullName(), account.getPreferredEmail());
  }

  private Account getCurrentUser() {
    CurrentUser u = user.get();
    if (u.isIdentifiedUser()) {
      return ((IdentifiedUser) u).getAccount();
    }
    return null;
  }
}
