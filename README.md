SEoIPServer
===========

Secure Element over IP server

Command list

AUTH <password> - Auth with password.<br .>
LIST - List card readers present on system.<br />
LOCK &lt;CARDRDR_NUM&gt; - Select and lock card reader.<br />
UNLOCK - Unlock acquired card reader.<br />
APDU &lt;APDU command in hex form. e.g. 00 A4 04 00 07 D4 10 00 00 03 00 01&gt; - Send apdu <br />
PING - Keepalive<br />
PONG - Keepalive<br />

Response list

LIST &lt;CARDRDR_NUM&gt; &lt;ARDRDR_NAME&gt; - Card reader list<br />
ENDLIST - End of cardreader list<br />
APDU &lt;APDU command response in hex form. e.g. 90 00&gt; - Response APDU<br />
OK - Command is successful<br />
ERROR &lt;ERROR_DESC&gt; - Command is failed.<br />
PING - Keepalive response<br />
PONG - Keepalive response<br />

This project is released under GPLv3.
