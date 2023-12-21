class Game{
    GameState gameState = GameState.TITLE;
    boolean initGameState = true;
    boolean run = true;
    boolean error = false;
    String[] ennemyList = new String[] {"WOLF","WOLF","WOLF"};
    
    SpellBook theBook;
    Unit playerUnit;
    Unit enemyUnit;
}