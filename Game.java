class Game{
    GameState gameState = GameState.TITLE;
    boolean initGameState = true;
    boolean run = true;
    boolean error = false;
    String[] ennemyList = new String[] {"WOLF","WOLF","WOLF"};
    Question[] questionList;
    
    SpellBook theBook;
    Unit playerUnit;
    Unit enemyUnit;
}