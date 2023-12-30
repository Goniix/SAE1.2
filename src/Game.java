class Game{
    GameState gameState = GameState.TITLE;
    boolean initGameState = true;
    boolean run = true;
    boolean error = false;
    boolean debug = false;
    String[] ennemyList = new String[] {"WOLF","WOLF","WOLF"};
    Question[] questionList;
    Question currentQuestion;
    
    SpellBook theBook;
    Unit playerUnit;
    Unit enemyUnit;
    int enemyNextAttack;
}