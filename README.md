Navn: Vegard van Jole \
Nicholas Uribarri,



### Instruksjoner:

#### SSH
>\$ ssh-keygen -t ed25519 \
>Skriv inn filsti \
>Hopp over passord \
>Gå til GitHub.uio.no \
>Gå til Settings -> SSH / GPG keys \
>Kopier SSH nøkkel til clipboard \
>macOS: \$ pbcopy < ~/.ssh/id_ed25519.pub \
>Windows (PowerShell or Git Bash): \$ cat ~/.ssh/id_ed25519.pub | clip or \$ clip < ~/.ssh/id_ed25519.pub \
>Lim inn nøkkelen du generate i terminalen

#### Git

>\$ git clone git@github.uio.no:IN2000-V26/team-12.git \
>Skriv navn inn i README.md\
>\$ git status (sjekk at det har sjedd en endring)\
>\$ git add . (legger til alle filer som er modifisert eller untracked)\
>\$ git status (sjekk at filen er updatert) \
>\$ git commit -m \"\<Navn\> commit\"
