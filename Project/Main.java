import cs2030.simulator.Initializer;
import cs2030.simulator.Simulator;
import java.util.Scanner;

/**
 * The main class reads in data and drives the simulation
 * @author Wang Pei
 */
class Main {
    public static void main(String[] args) {
        // read in data
        Scanner sc = new Scanner(System.in);
        int seed  = sc.nextInt();
        int serverNumber = sc.nextInt();
        int selfCheckoutNumber = sc.nextInt();
        int maxQLen = sc.nextInt();
        int customerNumber = sc.nextInt();
        double lambda = sc.nextDouble();
        double mu = sc.nextDouble();
        double rho = sc.nextDouble();
        double Pr = sc.nextDouble();
        double Pg = sc.nextDouble();
        sc.close();
        
        // initialize a discrete event simulator with raw data
        Simulator des = Initializer.init(
                serverNumber, selfCheckoutNumber,
                customerNumber, 
                maxQLen, 
                seed, lambda, mu, rho,
                Pr,
                Pg);
        
        // run simulation
        des.run();
        
        // print simulation result
        des.printResult();
    }
}
