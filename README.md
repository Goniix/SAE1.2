SpellAsker (Splask)

Développé par Nathanael COLLUMEAU, Gabriel REDOUIN-INNECCO
Contacts : dxnkmeme@gmail.com, gabriel.redouin@gmail.com

# Présentation de Splask
Splask est un RPG deckbuilding au tout par tour, où vous vous battez à coup de sorts et parrez les terribles questions de vos adversaires!
A votre tour, lancez un sort, puis répondez à la question de votre adversaire. Si vous avez bon, vous subissez moins de dégats!

Des captures d'écran illustrant le fonctionnement du logiciel sont proposées dans le répertoire shots.


# Utilisation de Splask
(La sauvegarde des highscore n'est pas fonctionnelle à cause d'une erreur avec la fonction saveCSV. la fonction saveScore est donc commentée, même si appellée à plusieurs endroits dans le code
Je ne suis pas parvenu à debug, l'erreur obtenue était un simple "stackoverflow" sans plus de précision. merci ça m'aide pas)

Afin d'utiliser le projet, il suffit de taper les commandes suivantes dans un terminal :

```
./compile.sh
```
(si ça ne marche pas lancez ```sed -i -e 's/\r$//' compile.sh```)
Permet la compilation des fichiers présents dans 'src' et création des fichiers '.class' dans 'classes'

```
./run.sh
```
(si ça ne marche pas lancez ```sed -i -e 's/\r$//' run.sh```)
Permet le lancement du jeu
