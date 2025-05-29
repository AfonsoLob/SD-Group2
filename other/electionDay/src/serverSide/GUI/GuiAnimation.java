package serverSide.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
<<<<<<< HEAD

=======
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Class responsible for handling voter animation in the GUI
 */
public class GuiAnimation {
    private ElectionAnimationPanel animationPanel;
    private Timer animationTimer;
    // Increase base delay to slow down animation
    private static final int ANIMATION_DELAY = 100; // Increased from 50 to 100 milliseconds
    
    public GuiAnimation() {
        createAnimationPanel();
    }
    
    /**
     * Create the animation panel
     */
    private void createAnimationPanel() {
        animationPanel = new ElectionAnimationPanel();
        
        // Make animation panel larger now that it's the main content
        animationPanel.setPreferredSize(new Dimension(1200, 500));
        
        animationPanel.setBorder(GuiStyles.createTitledBorder("Voter Journey Animation", 
                                                            javax.swing.border.TitledBorder.CENTER));
        
        // Set up the animation timer with speed-controlled refresh rate
        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            // Adjust animation refresh rate based on simulation speed:
            // - Slower speed = longer delay between frames = slower animation
            // - Faster speed = shorter delay between frames = faster animation
            // - Multiplier of 2 ensures animation is generally slower than real-time
            //   for better visibility of voter movement
            float speed = Gui.getStaticSimulationSpeed();
            int newDelay = Math.max(50, Math.round((ANIMATION_DELAY * 2) / speed));
            
            // Only update timer delay if it actually changed
            if (animationTimer.getDelay() != newDelay) {
                animationTimer.setDelay(newDelay);
            }
            animationPanel.repaint();
        });
        animationTimer.start();
    }
    
    /**
     * Get the animation panel
     */
    public JPanel getAnimationPanel() {
        return animationPanel;
    }
    
    /**
     * Clear all voters from the animation
     */
    public void clearVoters() {
        if (animationPanel != null) {
            animationPanel.clearVoters();
        }
    }
    
    /**
     * Reset animation state for simulation restart
     */
    public void resetAnimation() {
        clearVoters();
    }
    
    /**
     * Notify that a voter has arrived
     */
    public void voterArrived(int voterId) {
        if (animationPanel != null) {
            animationPanel.voterArrived(voterId);
        }
    }
    
    /**
     * Notify that a voter is entering the queue
     */
    public void voterEnteringQueue(int voterId) {
        if (animationPanel != null) {
            animationPanel.voterEnteringQueue(voterId);
        }
    }
    
    /**
     * Notify about voter validation result
     */
    public void voterValidated(int voterId, int valid) {
        if (animationPanel != null) {
            animationPanel.voterValidated(voterId, valid);
        }
    }
    
    /**
     * Notify about voter's vote
     */
    public void voterVoting(int voterId, boolean voteA) {
        if (animationPanel != null) {
            animationPanel.voterVoting(voterId, voteA);
        }
    }
    
    /**
     * Notify about exit poll response
     */
    public void voterExitPoll(int voterId, String vote) {
        if (animationPanel != null) {
            animationPanel.voterExitPoll(voterId, vote);
        }
    }
    
    /**
     * Notify about voter rebirth
     */
    public void voterReborn(int oldId, int newId) {
        if (animationPanel != null) {
            animationPanel.voterReborn(oldId, newId);
        }
    }
    
    /**
     * Inner class to represent the animation panel
     */
    class ElectionAnimationPanel extends JPanel {
<<<<<<< HEAD
        private static final long serialVersionUID = 1L; // Added serialVersionUID
        private transient Map<Integer, VoterInfo> voters = new HashMap<>(); // Marked transient
        private transient Map<Integer, Long> voterRemovalTimes = new HashMap<>(); // Marked transient
        private Random random = new Random();
        private transient Map<VoterStage, Integer> stageOccupancy = new HashMap<>();  // Marked transient
=======
        private Map<Integer, VoterInfo> voters = new HashMap<>();
        private Map<Integer, Long> voterRemovalTimes = new HashMap<>(); // Track when to remove voters
        private Random random = new Random();
        private Map<VoterStage, Integer> stageOccupancy = new HashMap<>();  // Track occupancy for each stage
>>>>>>> 77f28c76b37344e86d5129b3036572a92e56ad87
        private static final int VOTER_SIZE = 30;  // Size of voter circle
        private static final int VERTICAL_SPACING = 40;  // Minimum vertical spacing between voters
        private static final long REMOVAL_DELAY = 10000; // Increase from 5 to 10 seconds for longer visibility
        
        // Stages for the animation (X positions)
        // Will be calculated dynamically in paintComponent
        private int outsideX;
        private int queueX;
        private int validationX;
        private int votingX;
        private int exitPollX;
        private int rebirthX;
        private int panelHeight;
        private int stageWidth = 80;
        
        public ElectionAnimationPanel() {
            setBackground(Color.WHITE);
            // Initialize occupancy counts for each stage
            for (VoterStage stage : VoterStage.values()) {
                stageOccupancy.put(stage, 0);
            }
            
            // Create a timer to clean up old voters
            new Timer(1000, e -> removeExpiredVoters()).start();
        }
        
        /**
         * Remove voters that have been in exit poll or rebirth stages for too long
         */
        private void removeExpiredVoters() {
            long currentTime = System.currentTimeMillis();
            synchronized(voters) {
                List<Integer> toRemove = new ArrayList<>();
                for (Map.Entry<Integer, Long> entry : voterRemovalTimes.entrySet()) {
                    if (currentTime - entry.getValue() > REMOVAL_DELAY) {
                        int voterId = entry.getKey();
                        // Get the stage before removing to update occupancy
                        VoterInfo voter = voters.get(voterId);
                        if (voter != null) {
                            stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                            toRemove.add(voterId);
                        }
                    }
                }
                
                // Remove the expired voters
                for (int voterId : toRemove) {
                    voters.remove(voterId);
                    voterRemovalTimes.remove(voterId);
                }
            }
            repaint();
        }
        
        public void clearVoters() {
            synchronized(voters) {
                voters.clear();
                voterRemovalTimes.clear();
                // Reset all occupancy counts
                for (VoterStage stage : stageOccupancy.keySet()) {
                    stageOccupancy.put(stage, 0);
                }
            }
            repaint();
        }
        
        /**
         * Find an available vertical position that doesn't overlap with other voters at the same stage
         */
        private int findAvailableYPosition(VoterStage stage, int height) {
            // Create a map of occupied Y positions for this stage
            Map<Integer, Boolean> occupiedPositions = new HashMap<>();
            
            // Mark positions of existing voters in the same stage as occupied
            synchronized(voters) {
                for (VoterInfo voter : voters.values()) {
                    if (voter.stage == stage) {
                        // Mark the position and a buffer zone around it
                        for (int y = voter.y - VERTICAL_SPACING/2; y <= voter.y + VERTICAL_SPACING/2; y++) {
                            occupiedPositions.put(y, true);
                        }
                    }
                }
            }
            
            // Calculate usable area
            int topMargin = 50;
            int bottomMargin = 30;
            int availableHeight = height - topMargin - bottomMargin;
            
            // Try to find a free position by sampling
            for (int attempt = 0; attempt < 10; attempt++) {
                int y = topMargin + random.nextInt(Math.max(1, availableHeight));
                boolean positionClear = true;
                
                // Check if this position is clear (no occupancy within spacing)
                for (int offset = -VERTICAL_SPACING/2; offset <= VERTICAL_SPACING/2; offset++) {
                    if (occupiedPositions.containsKey(y + offset)) {
                        positionClear = false;
                        break;
                    }
                }
                
                if (positionClear) {
                    return y;
                }
            }
            
            // If we couldn't find a free position by sampling, place systematically
            int occupancy = stageOccupancy.get(stage);
            int yPos = topMargin + (occupancy * VERTICAL_SPACING) % (availableHeight - VERTICAL_SPACING);
            stageOccupancy.put(stage, occupancy + 1);  // Increment occupancy for this stage
            return yPos;
        }
        
        public void voterArrived(int voterId) {
            synchronized(voters) {
                VoterInfo voter = new VoterInfo();
                voter.id = voterId;
                voter.stage = VoterStage.OUTSIDE;
                voter.x = getWidth() / 8; // Will be adjusted in paintComponent
                // Find position that doesn't overlap with existing voters
                voter.y = findAvailableYPosition(VoterStage.OUTSIDE, getHeight());
                voter.color = new Color(
                    100 + random.nextInt(155),
                    100 + random.nextInt(155),
                    100 + random.nextInt(155)
                );
                voters.put(voterId, voter);
            }
            repaint();
        }
        
        public void voterEnteringQueue(int voterId) {
            synchronized(voters) {
                VoterInfo voter = voters.get(voterId);
                if (voter != null) {
                    // Update stage and reduce occupancy for old stage
                    stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                    
                    voter.stage = VoterStage.QUEUE;
                    voter.x = getWidth() / 8 * 2; // Will be adjusted in paintComponent
                    voter.y = findAvailableYPosition(VoterStage.QUEUE, getHeight());
                }
            }
            repaint();
        }
        
        public void voterValidated(int voterId, int valid) {
            synchronized(voters) {
                boolean validp = (valid == 1);
                VoterInfo voter = voters.get(voterId);
                if (voter != null) {
                    // Update stage and reduce occupancy for old stage
                    stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                    
                    VoterStage newStage = validp ? VoterStage.VALIDATION : VoterStage.REJECTED;
                    voter.stage = newStage;
                    voter.x = getWidth() / 8 * 3; // Will be adjusted in paintComponent
                    voter.y = findAvailableYPosition(newStage, getHeight());
                }
            }
            repaint();
        }
        
        public void voterVoting(int voterId, boolean voteA) {
            synchronized(voters) {
                VoterInfo voter = voters.get(voterId);
                if (voter != null) {
                    // Update stage and reduce occupancy for old stage
                    stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                    
                    voter.stage = VoterStage.VOTING;
                    voter.voteA = voteA;
                    voter.x = getWidth() / 8 * 4; // Will be adjusted in paintComponent
                    voter.y = findAvailableYPosition(VoterStage.VOTING, getHeight());
                }
            }
            repaint();
        }
        
        public void voterExitPoll(int voterId, String vote) {
            synchronized(voters) {
                VoterInfo voter = voters.get(voterId);
                if (voter != null) {
                    // Update stage and reduce occupancy for old stage
                    stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                    
                    voter.stage = VoterStage.EXIT_POLL;
                    voter.x = getWidth() / 8 * 5; // Will be adjusted in paintComponent
                    voter.y = findAvailableYPosition(VoterStage.EXIT_POLL, getHeight());
                    if (!vote.isEmpty()) {
                        voter.exitPollVote = "A".equals(vote);
                    }
                    
                    // Schedule this voter for removal
                    voterRemovalTimes.put(voterId, System.currentTimeMillis());
                }
            }
            repaint();
        }
        
        public void voterReborn(int oldId, int newId) {
            synchronized(voters) {
                VoterInfo voter = voters.get(oldId);
                if (voter != null) {
                    // Update stage and reduce occupancy for old stage
                    stageOccupancy.put(voter.stage, Math.max(0, stageOccupancy.get(voter.stage) - 1));
                    voter.stage = VoterStage.REBIRTH_ORIGIN;
                    voter.x = getWidth() / 8 * 6; // Will be adjusted in paintComponent
                    voter.y = findAvailableYPosition(VoterStage.REBIRTH_ORIGIN, getHeight());
                    
                    // Schedule the old voter for removal
                    voterRemovalTimes.put(oldId, System.currentTimeMillis());
                    
                    // Create a new voter with the reborn ID
                    VoterInfo reborn = new VoterInfo();
                    reborn.id = newId;
                    reborn.stage = VoterStage.REBORN;
                    reborn.x = getWidth() / 8 * 6; // Will be adjusted in paintComponent
                    reborn.y = findAvailableYPosition(VoterStage.REBORN, getHeight());
                    reborn.color = new Color(
                        100 + random.nextInt(155),
                        100 + random.nextInt(155),
                        100 + random.nextInt(155)
                    );
                    
                    // Add the new voter and also schedule it for removal after showing briefly
                    voters.put(newId, reborn);
                    voterRemovalTimes.put(newId, System.currentTimeMillis());
                }
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Calculate dynamic positions
            outsideX = width / 7;
            queueX = width / 7 * 2;
            validationX = width / 7 * 3;
            votingX = width / 7 * 4;
            exitPollX = width / 7 * 5;
            rebirthX = width / 7 * 6;
            panelHeight = height - 60;
            stageWidth = Math.min(100, width / 9); // Limit width for small screens
            
            // Draw stage areas
            drawStageArea(g2d, outsideX, "Outside", new Color(230, 230, 230));
            drawStageArea(g2d, queueX, "Queue", new Color(255, 255, 200));
            drawStageArea(g2d, validationX, "Validation", new Color(200, 255, 200));
            drawStageArea(g2d, votingX, "Voting", new Color(200, 200, 255));
            drawStageArea(g2d, exitPollX, "Exit Poll", new Color(255, 200, 255));
            drawStageArea(g2d, rebirthX, "Rebirth", new Color(255, 230, 230));
            
            // Calculate arrow positions
            int centerY = height / 2;
            int arrowY = centerY;
            
            // Draw arrows
            g2d.setColor(Color.BLACK);
            drawArrow(g2d, outsideX + stageWidth/2, arrowY, queueX - stageWidth/2, arrowY);
            drawArrow(g2d, queueX + stageWidth/2, arrowY, validationX - stageWidth/2, arrowY);
            drawArrow(g2d, validationX + stageWidth/2, arrowY, votingX - stageWidth/2, arrowY);
            drawArrow(g2d, votingX + stageWidth/2, arrowY, exitPollX - stageWidth/2, arrowY);
            drawArrow(g2d, exitPollX + stageWidth/2, arrowY, rebirthX - stageWidth/2, arrowY);
            
            // Draw a loop arrow from rebirth back to outside
            int loopY1 = panelHeight - 30;
            int loopY2 = panelHeight - 10;
            g2d.drawLine(rebirthX, loopY1, outsideX, loopY1);
            g2d.drawLine(outsideX, loopY1, outsideX, loopY2);
            drawArrow(g2d, outsideX, loopY2, outsideX, centerY + 20);
            
            // Create a defensive copy of the voters collection to avoid ConcurrentModificationException
            Map<Integer, VoterInfo> votersCopy;
            synchronized(voters) {
                votersCopy = new HashMap<>(voters);
            }
            
            // Scale all voters to match the new positions
            for (VoterInfo voter : votersCopy.values()) {
                // Update voter positions based on stage
                switch (voter.stage) {
                    case OUTSIDE: voter.x = outsideX; break;
                    case QUEUE: voter.x = queueX; break;
                    case VALIDATION: case REJECTED: voter.x = validationX; break;
                    case VOTING: voter.x = votingX; break;
                    case EXIT_POLL: voter.x = exitPollX; break;
                    case REBIRTH_ORIGIN: case REBORN: voter.x = rebirthX; break;
                }
                
                // Draw the voter at its updated position
                drawVoter(g2d, voter);
            }
            
            // Display occupancy counts for debugging if needed
            /*
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.setColor(Color.BLACK);
            int y = 20;
            for (VoterStage stage : VoterStage.values()) {
                g2d.drawString(stage + ": " + stageOccupancy.get(stage), 10, y);
                y += 15;
            }
            */
            
            // Draw legend showing active and pending removal voters
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Active Voters: " + votersCopy.size(), 10, 20);
            g2d.drawString("Pending Removal: " + voterRemovalTimes.size(), 10, 40);
        }
        
        private void drawStageArea(Graphics2D g2d, int x, String label, Color bgColor) {
            int width = stageWidth;
            int height = panelHeight - 60;
            
            // Draw area
            g2d.setColor(bgColor);
            g2d.fillRect(x - width/2, 30, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x - width/2, 30, width, height);
            
            // Draw label
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, x - labelWidth/2, 25);
        }
        
        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
            g2d.drawLine(x1, y1, x2, y2);
            
            // Draw arrowhead
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 8;
            
            int x3 = (int)(x2 - arrowSize * Math.cos(angle - Math.PI/6));
            int y3 = (int)(y2 - arrowSize * Math.sin(angle - Math.PI/6));
            
            int x4 = (int)(x2 - arrowSize * Math.cos(angle + Math.PI/6));
            int y4 = (int)(y2 - arrowSize * Math.sin(angle + Math.PI/6));
            
            g2d.drawLine(x2, y2, x3, y3);
            g2d.drawLine(x2, y2, x4, y4);
        }
        
        private void drawVoter(Graphics2D g2d, VoterInfo voter) {
            int size = 30;
            
            // Draw the voter circle
            g2d.setColor(voter.color);
            g2d.fillOval(voter.x - size/2, voter.y - size/2, size, size);
            
            // Draw the outline
            switch (voter.stage) {
                case REJECTED:
                    g2d.setColor(Color.RED);
                    break;
                case REBIRTH_ORIGIN:
                    g2d.setColor(Color.ORANGE);
                    break;
                case REBORN:
                    g2d.setColor(Color.GREEN);
                    break;
                default:
                    g2d.setColor(Color.BLACK);
            }
            g2d.drawOval(voter.x - size/2, voter.y - size/2, size, size);
            
            // Draw ID
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String idStr = String.valueOf(voter.id);
            int textWidth = g2d.getFontMetrics().stringWidth(idStr);
            g2d.drawString(idStr, voter.x - textWidth/2, voter.y + 5);
            
            // Draw vote indication if in voting or exit poll stage
            if (voter.stage == VoterStage.VOTING) {
                g2d.setColor(voter.voteA ? Color.BLUE : Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(voter.voteA ? "A" : "B", voter.x - 3, voter.y - size/2 - 5);
            } else if (voter.stage == VoterStage.EXIT_POLL && voter.exitPollVote != null) {
                g2d.setColor(voter.exitPollVote ? Color.BLUE : Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(voter.exitPollVote ? "A" : "B", voter.x - 3, voter.y - size/2 - 5);
            }
        }
    }
    
    /**
     * Class to hold voter information for animation
     */
    class VoterInfo {
        int id;
        int x, y;
        VoterStage stage;
        Color color;
        Boolean voteA = null;
        Boolean exitPollVote = null;
    }
    
    /**
     * Enum for voter stages in the animation
     */
    enum VoterStage {
        OUTSIDE,
        QUEUE,
        VALIDATION,
        REJECTED,
        VOTING,
        EXIT_POLL,
        REBIRTH_ORIGIN,
        REBORN
    }
}
