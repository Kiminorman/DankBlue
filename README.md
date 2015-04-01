Kurssin tekoäly lopputyö,

Gitin käyttö komentoriviltä:
Aluksi:
1. git fetch "name" #namen tilalle  sitte se henkilö kenen repositorystä hakee muutokset
2. git merge "name"/master
# Ylempi kahesti, (sama molemmille toisille ryhmäläisille)
3. Kirjottele muutokset
4. git add -A # tai -A korvaa tiedostolla, jota muutti
5. git commit -m "viestisi mitä muutit"
6. git push

Pertulle:
git remote add kimi https://github.com/Kiminorman/DankBlue
git remote -v



Run käynnistää graafisen pelin.
Run_cmd ottaa komentoriviltä argumentit: tekoäly1 tekoäly2 vuoron_pituus
Run_games_cmd pelaa viralliset pelit käyttäen run_cmd:tä hyväksi. Tiedostoon syötettävä tekoälyt.
move_report siirtää raportit toiseen kansioon.