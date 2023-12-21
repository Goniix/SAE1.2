import extensions.CSVFile;
import extensions.File;
import ijava.Curses;
class Main extends Program{
    final String ASCIILINE = "-----------------------------------------------\n";
    final String DECK_FILE = "src/deckList.txt";
    
    //GENERAL METHODS--------------------------------------------------------------------------------------------
    int clamp(int val, int min, int max){
        return Math.max(min,Math.min(max,val));
    }

    int lineCount(String fileName){
        File importedFile = newFile(fileName);

        String line = readLine(importedFile);

        int res = 0;
        while(line != null){
            line = readLine(importedFile);
            res++;
        }
        return res;
    }

    int max(int numb1, int numb2){
        return (numb1>numb2) ? numb1 : numb2;
    }

    char getPlayerInput(char max){
        char res;
        do{
            res = readChar();
        }while(res>max || res<'1');
        return res;
    }

    int[] shuffle(int[] list){
        int len = length(list);
        for(int index = 0; index<len; index++){
            int randomIndex = (int) (random()*len);
            int temp = list[index];
            list[index] = list[randomIndex];
            list[randomIndex] = temp;
        }
        return list;
    }

    void println(String[] list){
        String res = "[";
        for(int index = 0; index<length(list); index++){
            res+=list[index]+", ";
        }
        println(res+"]");
    }

    void println(int[] list){
        String res = "[";
        for(int index = 0; index<length(list); index++){
            res+=list[index]+", ";
        }
        println(res+"]");
    }

    int[] copy(int[] list){
        int[] res = new int[length(list)];
        for(int index = 0; index<length(list); index++){
            res[index] = list[index];
        }
        return res;
    }

    //GAMESTATE INPUTS--------------------------------------------------------------------------------------------
    void input(char key, Game game){
        switch(game.gameState){
            case TITLE:
                switch(key){
                    case '1':
                        break;
                    case '2':
                        switchGameState(GameState.SPELLIST,game);
                        break;
                    case '3':
                        game.run = false;
                        break;
                }
                break;
            case SPELLIST:
                switch(key){
                    case '1':
                        switchGameState(GameState.TITLE,game);
                        break;
                }
                break;
        }
    }

    void switchGameState(GameState state, Game game){
        game.gameState = state;
        game.initGameState = true;
        clearScreen();
    }

    //ABILITY METHODS--------------------------------------------------------------------------------------------
    Ability newAbility(String effect, int power, String target){
        //converts string loaded data from spellList into ability class objects
        Ability res = new Ability();
        Effect type;
        switch(effect){
            case "DMG":
                type = Effect.DAMAGE;
                break;
            case "HEL":
                type = Effect.HEAL;
                break;
            case "SHD":
                type = Effect.SHIELD;
                break;
            default:
                println("Error malformed effect: "+effect);
                type = null;
                break;
        }
        res.effectType = type;
        res.power = power;
        
        Target targ;
        switch(target){
            case "SLF":
                targ = Target.SELF;
                break;
            case "FOE":
                targ = Target.FOE;
                break; 
            default:
                println("Error malformed target: "+target);
                targ = null;
                break;
        }
        res.targetType = targ;
        return res; 
    }

    String toString(Ability ability){
        String res = "";
        res+= ability.power+" ";
        res+= toString(ability.effectType);
        res+=" to ";
        res+= toString(ability.targetType);

        return res;
    }

    void executeAbility(Ability ability, Unit targetUnit){
        int power = ability.power;
        Effect type = ability.effectType;
        switch(type){
            case DAMAGE:
                power -= targetUnit.shield;
                targetUnit.shield -= ability.power-power;
                targetUnit.health -= power;
                break;
            case HEAL:
                targetUnit.health = clamp(targetUnit.health+power,0,targetUnit.maxHealth);
                break;
            case SHIELD:
                targetUnit.shield += power;
                break;
        }
    }

    //UNIT METHODS--------------------------------------------------------------------------------------------
    Unit newUnit(String name){
        Unit res = new Unit();
        res.name = name;
        res.maxHealth = 100;
        res.health = res.maxHealth;
        res.shield = 0;
        res.hand = new int[] {-1,-1,-1,-1};
        return res;
    }

    String toString(Unit unit){
        String res = ASCIILINE;
        res+= "Name: "+ unit.name +"\n";
        res+= "Health: "+ (unit.health+unit.shield) + " / " + unit.maxHealth + "\n";
        res+= ASCIILINE;
        return res;
    }

    String[] readDeckFile(String fileName, String deckID){
    //imports deck list from file into a list of String (named deckList)
        File importedFile = newFile(fileName);

        String line;
        do{
            line = readLine(importedFile);
        }while(line != null && !line.equals(deckID));

        int deckSize = (line!=null) ? Integer.parseInt(readLine(importedFile)) : 0;
        String[] res = new String[deckSize];

        for(int index = 0; index<deckSize; index++){
            res[index] = readLine(importedFile);
        }

        return res;
    }

    int[] readDeckList(String[] stringedList, SpellBook book){
    //imports spell id from list of String (deckList)
        int stringedLen = length(stringedList);
        int[] res = new int[stringedLen];
        for(int index = 0; index<stringedLen; index++){
            res[index] = getSpellIndex(book,stringedList[index]);
        }
        return res;
    }

    void importDeck(Unit unit, String deckFileName, SpellBook book){
        unit.baseDeck = readDeckList(readDeckFile(deckFileName,unit.name),book);
        unit.deck = copy(unit.baseDeck);
    }

    void drawACard(Unit unit, int count){
        int index = 0;
        while(index<length(unit.hand) && count>0){
            int nextDeckCard = unit.deck[unit.deckIndex];
            if (unit.hand[index] == -1){
                unit.hand[index] = nextDeckCard;
                count--;
                unit.deckIndex++;
                if(unit.deckIndex==length(unit.deck)){
                    unit.deckIndex = 0;
                    shuffle(unit.deck);
                }
            }
            index++;
        }
    }

    void resetDeck(Unit unit){
        unit.deck = copy(unit.baseDeck);
        unit.hand = new int[] {-1,-1,-1,-1};
    }

    void discardACard(Unit unit, int index){
        unit.hand[index]=-1;
    }

    //EFFECT METHODS--------------------------------------------------------------------------------------------
    String toString(Effect type){
        String res = "";
        switch(type){
            case DAMAGE:
                res="damage";
                break;
            case HEAL:
                res="heal";
                break;
            case SHIELD:
                res="shield";
                break;
        }
        return res;
    }

    //TARGET METHODS--------------------------------------------------------------------------------------------
    String toString(Target type){
        String res = "";
        switch(type){
            case SELF:
                res="self";
                break;
            case FOE:
                res="foe";
                break;
        }
        return res;
    }

    Unit targetSwitch(Target type, Unit self, Unit foe){
        Unit res = null;
        switch(type){
            case SELF:
                res = self;
                break;
            case FOE:
                res = foe;
                break;
        }
        return res;
    }

    //SPELL METHODS--------------------------------------------------------------------------------------------
    Spell newSpell(String name, Ability[] abilities){
        Spell res = new Spell();
        res.name = name;
        res.spellAbilities = abilities;
        return res;
    }

    String toString(Spell spell){
        String res = "";
        res +=spell.name+" : ";
        for(int i = 0; i<length(spell.spellAbilities); i++){
            res+=toString(spell.spellAbilities[i])+", ";
        }
        return res;
    }

    void castSpell(Spell spell,Unit self, Unit foe){
        println("-> Casted "+spell.name+" !");
        int abilityCount = length(spell.spellAbilities);
        for(int i = 0; i<abilityCount; i++){
            Ability ability = spell.spellAbilities[i];
            Unit targetUnit = targetSwitch(ability.targetType,self,foe);
            executeAbility(ability,targetUnit);
        }
    }

    //SPRITE METHODS--------------------------------------------------------------------------------------------
    Sprite newEmptySprite(int width, int height){
        Sprite res = new Sprite();
        res.width = width;
        res.height = height;
        res.image = new String[height];
        return res;
    }

    Sprite importSprite(String fileName){
        File importedFile = newFile(fileName);
        int lineNumber = lineCount(fileName);

        Sprite res = new Sprite();
        res.image = new String[lineNumber];
        res.height = lineNumber;
        int maxWidth = 0;


        String line = readLine(importedFile);

        int index = 0;
        while(line!=null){
            res.image[index] = line;
            maxWidth = max(maxWidth,length(line));
            line = readLine(importedFile);

            index++;
        }
        res.width = maxWidth;
        return res;
    }

    String toString(Sprite sprite){
        String res = "";
        for(int index = 0; index<sprite.height; index++){
            res += sprite.image[index]+"\n";
        }
        return res;
    }

    Sprite castSprite(Sprite targetSprite, Sprite sourceSprite, int x, int y){
        //casts a sourceSprite into a targetSprite
        Sprite res = newEmptySprite(targetSprite.width,targetSprite.height);
        for(int idy = 0; idy<targetSprite.height; idy++){
            res.image[idy]="";
            for(int idx = 0; idx<targetSprite.width; idx++){
                if((idx>=x && idx<x+sourceSprite.width)&&(idy>=y && idy<y+sourceSprite.height)){
                    res.image[idy]+= sourceSprite.image[idy-y].charAt(idx-x);
                }
                else res.image[idy] += targetSprite.image[idy].charAt(idx);
            }
        }
        return res;
    }

    Sprite castSpriteExp(Sprite targetSprite, Sprite sourceSprite, int x, int y){
        //casts a sourceSprite into a targetSprite, targetSprite is enlarged if it is too small for the casted sprite(sprite casted at negative coordinates will not show up)
        int widthOverflow = max((x + sourceSprite.width), targetSprite.width);
        int heightOverflow = max((y + sourceSprite.height), targetSprite.height);

        Sprite res = newEmptySprite(widthOverflow,heightOverflow);
        for(int idy = 0; idy<heightOverflow; idy++){
            res.image[idy]="";
            for(int idx = 0; idx<widthOverflow; idx++){
                if((idx>=x && idx<x+sourceSprite.width)&&(idy>=y && idy<y+sourceSprite.height)){
                    res.image[idy]+= sourceSprite.image[idy-y].charAt(idx-x);
                }
                else{
                    char appenedChar = ' ';
                    if ((idx>=0 && idx<targetSprite.width)&&(idy>=0 && idy<targetSprite.height)){
                        appenedChar = targetSprite.image[idy].charAt(idx);
                    }
                    res.image[idy] += appenedChar;
                }
            }
        }
        return res;
    }

    //SPELLBOOK METHODS--------------------------------------------------------------------------------------------
    SpellBook initialiseSpellBook(){
        SpellBook res = new SpellBook();
        CSVFile loadedSpells = loadCSV("src/spellList.csv",',');
        res.allSpells = new Spell[rowCount(loadedSpells)-1];
        for(int i = 0; i<rowCount(loadedSpells)-1; i++){
            String name = getCell(loadedSpells,i+1,0);
            Ability[] effects = importAbilities(getCell(loadedSpells,i+1,1)) ;
            res.allSpells[i] = newSpell(name,effects);
        }
        return res;
    }

    String toString(SpellBook book){
        String res = ASCIILINE;
        res+="List of all available spells:\n";
        for(int i = 0; i<length(book.allSpells); i++){
            res+=i+": "+toString(book.allSpells[i])+"\n";
        }
        res+= ASCIILINE;
        return res;
    }

    Ability[] importAbilities(String data){
        //parses a stringed list of abilities dumped from spellList.csv, and put them into a list 
        int abilityCount = 0;
        for(int i = 0; i<length(data);i++){
            if(data.charAt(i)==';') abilityCount++;
        }

        Ability[] res = new Ability[abilityCount];
        
        for(int i = 0;i<abilityCount;i++){
            int strOffset = i*12;
            String effect = data.substring(strOffset,strOffset+3);
            int power = Integer.parseInt(data.substring(strOffset+4,strOffset+7));
            String target = data.substring(strOffset+8,strOffset+11);
            res[i] = newAbility(effect,power,target);
        }

        return res;
    }

    int getSpellIndex(SpellBook book, String spellName){
        int index = 0;
        while(index<length(book.allSpells) && !book.allSpells[index].name.equals(spellName)){
            index++;
        }
        return (index==length(book.allSpells)) ? -1 : index;
    }


    //MAINLOOP--------------------------------------------------------------------------------------------
    void algorithm(){
        clearScreen();
        Game game = new Game();
        SpellBook theBook = initialiseSpellBook();

        // println(toString(theBook));

        Unit playerUnit = newUnit("PLAYER");
        Unit enemyUnit = null;


        // println(toString(playerUnit));
        // castSpell(theBook.allSpells[5],playerUnit,enemyUnit);
        // println(toString(playerUnit));

        Sprite titleScreen = importSprite("src/spellAskerTitle.txt");
        Sprite blankSquare = importSprite("src/blankSquare.txt");

        importDeck(playerUnit,DECK_FILE,theBook);
        println(playerUnit.deck);

        int testIndex = 0;
        show();
        while(game.run){
            
            try{
                clearScreen();
                print(toString(castSprite(titleScreen, blankSquare,testIndex%titleScreen.width,0)));
                Thread.sleep(16);
            }
            catch(InterruptedException e){
                println(e);
            }
            testIndex++;
            
            /*
            switch(game.gameState){
                case TITLE:
                    if(game.initGameState){
                        println(toString(titleScreen));
                        println("Choose an option");
                        println("1.Start Game");
                        println("2.Show List of spells");
                        println("3.Exit Game");
                        game.initGameState = false;
                    }
                    input(getPlayerInput('3'),game);
                    break;
                case MAP:
                    if(game.initGameState){

                        game.initGameState = false;
                    }
                    break;
                case COMBAT:
                    if(game.initGameState){
                        resetDeck(playerUnit);
                        shuffle(playerUnit.deck);
                        drawACard(playerUnit,4);

                        resetDeck(enemyUnit);
                        shuffle(enemyUnit.deck);
                        drawACard(enemyUnit,4);

                        game.initGameState = false;
                    }
                    break;
                case SHOP:
                    if(game.initGameState){
                        
                        game.initGameState = false;
                    }
                    break;
                case SPELLIST:
                    if(game.initGameState){
                        println(toString(theBook));
                        println("1.Go back to title");
                        game.initGameState = false;
                    }
                    input(getPlayerInput('1'),game);
                    break;
            }*/
        }
    }
}