package serverSide.main;

/**
 *    Definition of the simulation parameters.
 */

public final class SimulPar
{
  
  /**
   *   Max number of voters waiting in queue (between 2 and 5).
   */

   public static final int K = 3;

  /**
   *   Number of Voters (between 3 and 10).
   */

   public static final int N = 5;

  /**
   *   Number of entities requesting shutdown. ????????????????????????????
   */ 

   public static final int E = 2;

  /**
   *   Percentage of voters to be selected for exit poll (0.5 = 50%).
   */

   public static final double EXIT_POLL_PERCENTAGE = 0.5;

  /**
   *   Time for ID validation in milliseconds (between 5 and 10).
   */

   public static final int MIN_ID_VALIDATION_TIME = 5;
   public static final int MAX_ID_VALIDATION_TIME = 10;

  /**
   *   Time for voting in milliseconds (between 0 and 15).
   */

   public static final int MIN_VOTING_TIME = 0;
   public static final int MAX_VOTING_TIME = 15;

  /**
   *   Time for exit poll response in milliseconds (between 5 and 10).
   */

   public static final int MIN_EXIT_POLL_TIME = 5;
   public static final int MAX_EXIT_POLL_TIME = 10;

  /**
   *   Probability of a voter telling the truth in exit poll (0.8 = 80%).
   */

   public static final double TRUTH_PROBABILITY = 0.8;

  /**
   *   Probability of a voter wanting to answer exit poll (0.6 = 60%).
   */

   public static final double ANSWER_PROBABILITY = 0.6;

  /**
   *   Number of Max Votes
   */

   public static final int MAX_VOTES = 30;

  /**
   *   It can not be instantiated.
   */

   private SimulPar ()
   { throw new IllegalStateException("Utility class"); }

  /**
   *   Validates the simulation parameters.
   *   @throws IllegalArgumentException if any parameter is invalid
   */

  @SuppressWarnings("unused")
   public static void validateParameters()
   {
      if (K < 2 || K > 5)
      { throw new IllegalArgumentException("Queue size (K) must be between 2 and 5"); }
      if (N < 3 || N > 10)
      { throw new IllegalArgumentException("Number of voters (N) must be between 3 and 10"); }
      if (EXIT_POLL_PERCENTAGE < 0 || EXIT_POLL_PERCENTAGE > 100)
      { throw new IllegalArgumentException("Exit poll percentage must be between 0 and 100"); }
      if (MIN_ID_VALIDATION_TIME < 5 || MAX_ID_VALIDATION_TIME > 10 || MIN_ID_VALIDATION_TIME > MAX_ID_VALIDATION_TIME)
      { throw new IllegalArgumentException("ID validation time must be between 5 and 10ms"); }
      if (MIN_VOTING_TIME < 0 || MAX_VOTING_TIME > 15 || MIN_VOTING_TIME > MAX_VOTING_TIME)
      { throw new IllegalArgumentException("Voting time must be between 0 and 15ms"); }
      if (MIN_EXIT_POLL_TIME < 5 || MAX_EXIT_POLL_TIME > 10 || MIN_EXIT_POLL_TIME > MAX_EXIT_POLL_TIME)
      { throw new IllegalArgumentException("Exit poll time must be between 5 and 10ms"); }
      if (TRUTH_PROBABILITY < 0 || TRUTH_PROBABILITY > 1)
      { throw new IllegalArgumentException("Truth probability must be between 0 and 1"); }
      if (ANSWER_PROBABILITY < 0 || ANSWER_PROBABILITY > 1)
      { throw new IllegalArgumentException("Answer probability must be between 0 and 1"); }
   }
}
