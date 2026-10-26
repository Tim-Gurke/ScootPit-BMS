# ScootPit BMS 1.6.13-test.2

Korrigiert den Verbrauch „ab Reset“ bei Fahrtpausen. Die lokale BMS-Kopplung mit Watt’n’Pit aus test.1 bleibt enthalten.

Eine explizit ausgeschlossene Fahrtpause ohne Strecken-, Fahrzeit- oder Energiezuwachs wird nicht mehr allein wegen fehlender BMS-Pakete als Verbrauchslücke markiert. Beim Fortsetzen wird kein zusätzlicher künstlicher Lückenmarker mehr geschrieben. Auch das Beenden einer noch pausierten Fahrt wertet zunächst den eingefrorenen Pausenstand aus. Tatsächlich unvollständige Fahrabschnitte bleiben als unvollständig erkennbar.

Bereits gespeicherte Reset-Zähler und ihre Rohsummen werden nicht gelöscht oder nachträglich umgeschrieben. Weil alte Lückenmarker keine sichere Unterscheidung zwischen Pausen- und echten Messlücken erlauben, wird ein solcher Reset-Verbrauch mit „≈“ als unvollständiger Wert aus erfasster Energie / aufgezeichneter Strecke angezeigt. Fehlende Energie wird nicht ergänzt. Die Kachelnote und der Reset-Dialog erklären die Einschränkung. Bei noch fehlender Strecke bleibt die Anzeige ohne Durchschnittswert.

Als Update installieren und vorerst nichts zurücksetzen. Watt’n’Pit benötigt für diese Korrektur kein weiteres Update. Pause, BMS-Verbindungsunterbrechung und Fortsetzen sind anschließend am Roller zu prüfen.
