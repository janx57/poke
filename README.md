poke
====

A plugin for Google Gerrit which allows to "poke" reviewers who haven't submitted a score yet.

<h3>Description</h3>
Poke plugin lets a change owner or a patchset uploader to poke reviewers who haven’t submitted a non-zero score in the Code-Review category yet.

A button labeled “Poke” is placed on a change screen.

As a result of a poke, an e-mail will be sent with a link to the change.

<h3>Requirements</h3>
- Java 7
- Maven

<h3>Getting source code</h3>
<pre>git clone https://github.com/janx57/poke</pre>

<h3>Building</h3>
<pre>cd poke
mvn clean package</pre>

<h3>Deployment</h3>
Before deployment you have to copy an e-mail template src/main/resources/Mail/Poke.vm to $GERRIT_HOME/etc/mail

Afterwards, you deploy Poke as any regular Gerrit plugin.
