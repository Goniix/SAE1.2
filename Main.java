import extensions.CSVFile;
import extensions.File;
import ijava.Curses;
class Main extends Program{
    final String ASCIILINE = "-----------------------------------------------\n";
    
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
            case "PLR":
                targ = Target.PLAYER;
                break;
            case "FOE":
                targ = Target.ENNEMY;
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
    Unit newUnit(Target type, String name){
        Unit res = new Unit();
        res.name = name;
        res.targetType = type;
        res.maxHealth = 100;
        res.health = res.maxHealth;
        res.shield = 0;
        return res;
    }
    String toString(Unit unit){
        String res = ASCIILINE;
        res+= "Name: "+ unit.name +"\n";
        res+= "Health: "+ (unit.health+unit.shield) + " / " + unit.maxHealth + "\n";
        res+= ASCIILINE;
        return res;
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
            case PLAYER:
                res="player";
                break;
            case ENNEMY:
                res="ennemy";
                break;
        }
        return res;
    }
    Unit targetToUnit(Target type, Unit player, Unit foe){
        Unit res = null;
        switch(type){
            case PLAYER:
                res = player;
                break;
            case ENNEMY:
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
    void castSpell(Spell spell,Unit player, Unit foe){
        println("-> Casted "+spell.name+" !");
        int abilityCount = length(spell.spellAbilities);
        for(int i = 0; i<abilityCount; i++){
            Ability ability = spell.spellAbilities[i];
            Unit targetUnit = targetToUnit(ability.targetType,player,foe);
            executeAbility(ability,targetUnit);
        }
    }
    //SPRITE METHODS--------------------------------------------------------------------------------------------
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

    void algorithm(){
        GameState gameState = GameState.TITLE;
        boolean initGameState = true;
        boolean run = false;
        boolean error = false;

        SpellBook theBook = initialiseSpellBook();
        println(toString(theBook));
        Unit playerUnit = newUnit(Target.PLAYER,"Player");
        Unit ennemyUnit = null;
        println(toString(playerUnit));
        castSpell(theBook.allSpells[5],playerUnit,ennemyUnit);
        println(toString(playerUnit));
        Sprite titleScreen = importSprite("src/spellAskerTitle.txt");
        while(run){
            switch(gameState){
                case TITLE:
                    if(initGameState){

                    }
                    break;
                case MAP:
                    if(initGameState){
                        
                    }
                    break;
                case COMBAT:
                    if(initGameState){
                        
                    }
                    break;
                case SHOP:
                    if(initGameState){
                        
                    }
                    break;
            }
        }
    }
}