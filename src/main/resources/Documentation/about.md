Poke plugin lets a change owner or a patchset uploader to poke reviewers
who haven't submitted a non-zero score in the Code-Review category yet.

A button labeled "Poke" is placed on a change screen.

As a result of a poke, an e-mail will be sent with a link to the change.

WARNING: Currently an e-mail template Mail/Poke.vm must be copied to $GERRIT_HOME/etc/mail
before installing the plugin.

