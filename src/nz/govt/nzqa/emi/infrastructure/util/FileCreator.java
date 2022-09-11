package nz.govt.nzqa.emi.infrastructure.util;

/**
 * Creates binary files.
 * 
 * @author Alisdair Hamblyn
 */
public class FileCreator {
	
	/**
	 * The BigFileReader.
	 */
	private BigFileReader bfr;
	
	/*
	 * Constructor.
	 */
	public FileCreator() {
		bfr = new BigFileReader();
	}
	
	/**
	 * Main method.
	 * @param args the command line parameters.
	 */
	public static void main(String[] args) {
		FileCreator test = new FileCreator();
		test.run();
	}
	
	/**
	 * Creates the files.
	 */
	public void run() {
		// create test files
		try {
			String fileName = "data_1K.dat";
			bfr.createData(fileName, 100);
			System.out.println(fileName + " created");
/*			String fileName = "data_10K.dat";
			bfr.createData(fileName, 1000);
			System.out.println(fileName + " created");
			fileName = "data_100K.dat";
			bfr.createData(fileName, 10000);
			System.out.println(fileName + " created");
			fileName = "data_1M.dat";
			bfr.createData(fileName, 100000);
			System.out.println(fileName + " created");
			fileName = "data_2M.dat";
			bfr.createData(fileName, 200000);
			System.out.println(fileName + " created");
			fileName = "data_3M.dat";
			bfr.createData(fileName, 300000);
			System.out.println(fileName + " created");
			fileName = "data_4M.dat";
			bfr.createData(fileName, 400000);
			System.out.println(fileName + " created");
			fileName = "data_5M.dat";
			bfr.createData(fileName, 500000);
			System.out.println(fileName + " created");
			fileName = "data_6M.dat";
			bfr.createData(fileName, 600000);
			System.out.println(fileName + " created");
			fileName = "data_7M.dat";
			bfr.createData(fileName, 700000);
			System.out.println(fileName + " created");
			fileName = "data_8M.dat";
			bfr.createData(fileName, 800000);
			System.out.println(fileName + " created");
			fileName = "data_9M.dat";
			bfr.createData(fileName, 900000);
			System.out.println(fileName + " created");
			fileName = "data_10M.dat";
			bfr.createData(fileName, 1000000);
			System.out.println(fileName + " created");
			fileName = "data_20M.dat";
			bfr.createData(fileName, 2000000);
			System.out.println(fileName + " created");
			fileName = "data_30M.dat";
			bfr.createData(fileName, 3000000);
			System.out.println(fileName + " created");
			fileName = "data_40M.dat";
			bfr.createData(fileName, 4000000);
			System.out.println(fileName + " created");
			fileName = "data_50M.dat";
			bfr.createData(fileName, 5000000);
			System.out.println(fileName + " created");
			String fileName = "data_60M.dat";
			bfr.createData(fileName, 6000000);
			System.out.println(fileName + " created");
			fileName = "data_70M.dat";
			bfr.createData(fileName, 7000000);
			System.out.println(fileName + " created");
			fileName = "data_80M.dat";
			bfr.createData(fileName, 8000000);
			System.out.println(fileName + " created");
			fileName = "data_90M.dat";
			bfr.createData(fileName, 9000000);
			System.out.println(fileName + " created");
			fileName = "data_100M.dat";
			bfr.createData(fileName, 10000000);
			System.out.println(fileName + " created");
			fileName = "data_200M.dat";
			bfr.createData(fileName, 20000000);
			System.out.println(fileName + " created");
			fileName = "data_300M.dat";
			bfr.createData(fileName, 30000000);
			System.out.println(fileName + " created");
			fileName = "data_400M.dat";
			bfr.createData(fileName, 40000000);
			System.out.println(fileName + " created");
			fileName = "data_500M.dat";
			bfr.createData(fileName, 50000000);
			System.out.println(fileName + " created");*/
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
