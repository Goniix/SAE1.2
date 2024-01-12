SpellAsker (Splask)

Développé par Nathanael COLLUMEAU, Gabriel REDOUIN-INNECCO
Contacts : dxnkmeme@gmail.com, gabriel.redouin@gmail.com

# Présentation de Splask
Splask est un RPG deckbuilding au tout par tour, où vous vous battez à coup de sorts et parrez les terribles questions de vos adversaires!
A votre tour, lancez un sort, puis répondez à la question de votre adversaire. Si vous avez bon, vous subissez moins de dégats!

Des captures d'écran illustrant le fonctionnement du logiciel sont proposées dans le répertoire shots. (nan WIP)


# Utilisation de Splask
Les boucles, cycles d'états du jeu et import des données fonctionnent
La modification de deck, la détection de fin de combat et de fin de partie ne sont pas encore implémentés (le jeu ne s'arrête jamais on reste stuck dans un combat infinit)

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
