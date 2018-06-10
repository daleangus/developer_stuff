package com.angus.goldenegg.sectortrend_io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.angus.goldenegg.Environment;
import com.angus.goldenegg.LeastSquares;
import com.angus.goldenegg.sectortrend_io.dao.Dao;
import com.angus.goldenegg.sectortrend_io.domainmodel.HistoricalPriceIO;
import com.angus.goldenegg.sectortrend_io.domainmodel.PricingCalculation;
import com.angus.goldenegg.sectortrend_io.domainmodel.Stock;

public class CalculatedPricingData {
	static Logger logger = Logger.getLogger(CalculatedPricingData.class);

	private static DriverManagerDataSource dataSource;
	private Dao dao;

	public CalculatedPricingData() {
		Properties properties = Environment.getProperties();

		String mySqlUsername = properties.getProperty("mySqlUsername");
		String mySqlPassword = properties.getProperty("mySqlPassword");
		String mySqlUrl = properties.getProperty("mySqlUrl");

		dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(mySqlUrl);
		dataSource.setUsername(mySqlUsername);
		dataSource.setPassword(mySqlPassword);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			logger
					.info("Usage: CalculatedPricingData [PROPERTIES_FILE] [<-init> <dataid>] or [<-file> <filename>]");
			throw new Exception();
		}
		Properties properties = null;

		properties = new Properties();
		properties.load(new FileInputStream(args[0]));
		PropertyConfigurator.configure("log4j.goldenegg.nosmtp.properties");

		Environment.setProperties(properties);

		CalculatedPricingData cpd = new CalculatedPricingData();
		if (args.length == 1) // only properties file
			cpd.go();
		else if (args.length == 2) {// -init
			if ("-init".equals(args[1]))
				cpd.go2(null); // calculates past date
			else
				throw new Exception("Invalid argument");
		} else if (args.length == 3) {
			if ("-file".equals(args[1]) || "-f".equals(args[1])) {
				cpd.go3(args[2]);
			} else if ("-init".equals(args[1]))
				cpd.go2(args[2]);
		} else if (args.length == 4) {
			if (("-file".equals(args[1]) || "-f".equals(args[1]))
					&& "scpd".equals(args[3])) {
				cpd.goscpd(args[2]);
			}
		}
	}

	private void go3(String filename) {
		// file contains dataids
		dao = new Dao();
		dao.setDataSource(dataSource);

		FileInputStream fstream = null;
		DataInputStream dis = null;
		BufferedReader br = null;
		String strLine; // dataId

		List<Stock> stockList = new ArrayList<Stock>();
		ConcurrentLinkedQueue<Stock> queue = new ConcurrentLinkedQueue<Stock>();
		try {
			fstream = new FileInputStream(filename);
			dis = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(dis));

			while ((strLine = br.readLine()) != null) {
				List<Stock> stock = dao.getStock(strLine);
				if (stock.size() == 1) {
					System.out.println(strLine);
					stockList.add(stock.get(0));
				}
			}
			dis.close();
			br.close();
			fstream.close();

		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		queue.addAll(stockList);

		int maxThreadCount = 1;
		for (int i = 0; i < maxThreadCount; i++) {
			Processor p = new Processor(queue, i);
			Thread t = new Thread(p);
			t.start();
		}
	}

	private void goscpd(String filename) {
		// file contains dataids
		dao = new Dao();
		dao.setDataSource(dataSource);

		FileInputStream fstream = null;
		DataInputStream dis = null;
		BufferedReader br = null;
		String strLine; // dataId

		List<Stock> stockList = new ArrayList<Stock>();
		ConcurrentLinkedQueue<Stock> queue = new ConcurrentLinkedQueue<Stock>();
		try {
			fstream = new FileInputStream(filename);
			dis = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(dis));

			while ((strLine = br.readLine()) != null) {
				List<Stock> stock = dao.getStock(strLine);
				if (stock.size() == 1) {
					System.out.println(strLine);
					stockList.add(stock.get(0));
				}
			}
			dis.close();
			br.close();
			fstream.close();

		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		queue.addAll(stockList);

		List<HistoricalPriceIO> dateList = null;
		dateList = dao.getHistoricalPriceDates(stockList.get(1).getDataId());

		int maxThreadCount = 8;
		for (int i = 0; i < maxThreadCount; i++) {
			Processor p = new Processor(queue, i, dateList, false);
			Thread t = new Thread(p);
			t.start();
		}
	}

	private void go() {
		dao = new Dao();
		dao.setDataSource(dataSource);

		List<Stock> stockList = dao.getAllStockForCpd(false);

		ConcurrentLinkedQueue<Stock> queue = new ConcurrentLinkedQueue<Stock>();
		queue.addAll(stockList);

		int maxThreadCount = 5;
		for (int i = 0; i < maxThreadCount; i++) {
			Processor p = new Processor(queue, i);
			Thread t = new Thread(p);
			t.start();
		}
	}

	private void go2(String dataId) {
		dao = new Dao();
		dao.setDataSource(dataSource);

		List<Stock> stockList;
		if (dataId == null)
			stockList = dao.getAllStockForCpd(true);
		else
			stockList = dao.getStock(dataId);

		if (stockList.size() == 0)
			return; // no stock List to process

		List<HistoricalPriceIO> dateList = null;
		int n = 0;
		boolean foundList = false;
		while (n < stockList.size()) {

			dateList = dao
					.getHistoricalPriceDates(stockList.get(n).getDataId());

			if (dateList.size() >= 200) {
				foundList = true;
				break;
			} else {
				System.out.println("Historical prices is less than 200 ("
						+ dateList.size() + ") for "
						+ stockList.get(n).getDataId() + " "
						+ stockList.get(n).getTicker() + ".");
			}
			n++;
		}

		if (!foundList) {
			return; // nothing in the list has at least 200 dates
		}

		Collections.shuffle(stockList);
		ConcurrentLinkedQueue<Stock> queue = new ConcurrentLinkedQueue<Stock>();
		queue.addAll(stockList);

		int maxThreadCount = 5;
		for (int i = 0; i < maxThreadCount; i++) {
			Processor p = new Processor(queue, i, dateList,
					(dataId == null ? true : false));
			Thread t = new Thread(p);
			t.start();
		}
	}

	private class Processor implements Runnable {
		private ConcurrentLinkedQueue<Stock> queue;
		private Stock stock;
		private int threadId;
		private List<HistoricalPriceIO> histPriceList;
		private List<HistoricalPriceIO> dateList;
		private boolean bulkInitialization = false;

		public Processor(ConcurrentLinkedQueue<Stock> queue, int threadId) {
			this.queue = queue;
			this.threadId = threadId;

			dao = new Dao();
			dao.setDataSource(dataSource);
		}

		public Processor(ConcurrentLinkedQueue<Stock> queue, int threadId,
				List<HistoricalPriceIO> dateList, boolean bulkInitialization) {
			this.queue = queue;
			this.threadId = threadId;
			this.dateList = dateList;
			this.bulkInitialization = bulkInitialization;

			dao = new Dao();
			dao.setDataSource(dataSource);
		}

		@Override
		public void run() {
			while ((stock = queue.poll()) != null) {
				String dataId = stock.getDataId();

				// Allows multiple machines to run the bulk initialization
				// (multiple dataids)
				if (bulkInitialization) {
					// Check if still not initialized by other process
					List<Stock> stockList = dao
							.getNotInitializedStockForCpd(dataId);
					if (stockList.size() == 0) {
						System.out.println("Skipping " + dataId);
						continue;
					}
				}
				System.out.println("Doing " + threadId + ": " + dataId + " "
						+ stock.getTicker());

				if (dateList == null) {
					PricingCalculation pc = new PricingCalculation();
					pc.setDataId(dataId);
					Date today = new Date();
					pc.setCalcdate(today);
					// must be called from 200 to 10
					pc.setSlope200(calculateSlope(dataId, 200, today));
					pc.setSma200(calculateSMA(dataId, 200, today));
					pc.setSlope100(calculateSlope(dataId, 100, today));
					pc.setSma100(calculateSMA(dataId, 100, today));
					pc.setSlope50(calculateSlope(dataId, 50, today));
					pc.setSma50(calculateSMA(dataId, 50, today));
					pc.setSlope21(calculateSlope(dataId, 21, today));
					pc.setSma21(calculateSMA(dataId, 21, today));
					pc.setSlope10(calculateSlope(dataId, 10, today));
					pc.setSma10(calculateSMA(dataId, 10, today));
					pc.setAwesomeOscillator(calculateAwesomeOscillator(dataId,
							today));
					pc.setMomentum34(calculateMomentum34(dataId, today));
					pc.setTangentSlope(calculateTangentSlope(dataId, today));

					dao.saveCalculatedPricingData(pc);
					dao.markAsInitialized(pc);
				} else {
					PricingCalculation pc = null;
					for (HistoricalPriceIO hpio : dateList) {
						pc = new PricingCalculation();
						Date pastDate = hpio.getClosedate();
						pc.setDataId(dataId);
						pc.setCalcdate(pastDate);
						// must be called from 200 to 10
						pc.setSlope200(calculateSlope(dataId, 200, pastDate));
						pc.setSma200(calculateSMA(dataId, 200, pastDate));
						pc.setSlope100(calculateSlope(dataId, 100, pastDate));
						pc.setSma100(calculateSMA(dataId, 100, pastDate));
						pc.setSlope50(calculateSlope(dataId, 50, pastDate));
						pc.setSma50(calculateSMA(dataId, 50, pastDate));
						pc.setSlope21(calculateSlope(dataId, 21, pastDate));
						pc.setSma21(calculateSMA(dataId, 21, pastDate));
						pc.setSlope10(calculateSlope(dataId, 10, pastDate));
						pc.setSma10(calculateSMA(dataId, 10, pastDate));
						pc.setAwesomeOscillator(calculateAwesomeOscillator(
								dataId, pastDate));
						pc.setMomentum34(calculateMomentum34(dataId, pastDate));
						pc.setTangentSlope(calculateTangentSlope(dataId,
								pastDate));

						dao.saveCalculatedPricingData(pc);
					}
					dao.markAsInitialized(pc);
				}
			}
		}

		private Double calculateTangentSlope(String dataId, Date maxDate) {
			int numPoints = 21;

			List<HistoricalPriceIO> aoHistPriceList = null;
			aoHistPriceList = dao.selectHistoricalPricesDescLimit(dataId,
					numPoints, maxDate);

			if (aoHistPriceList.size() < numPoints)
				return null;

			Vector<Double> v = new Vector<Double>();
			for (HistoricalPriceIO hio : aoHistPriceList) {
				v.add(hio.getClose());
			}
			Collections.reverse(v);
			// for (Double d : v) {
			// System.out.println(d.doubleValue());
			// }

			LeastSquares ls = new LeastSquares(v, 2);
			// quadratic f(x) = ax^2 + bx + c
			// first derivative f'(x) = 2ax + b + c
			// Solve f'(z) = 2az + b
			double a = ls.getCoefficient(2);
			double b = ls.getCoefficient(1);

			double f = 2.0 * a * numPoints + b;

			return Double.valueOf(f);
		}

		private Double calculateMomentum34(String dataId, Date maxDate) {
			List<HistoricalPriceIO> aoHistPriceList = null;
			aoHistPriceList = dao.selectHistoricalPricesDescLimit(dataId, 34,
					maxDate);

			if (aoHistPriceList.size() < 34)
				return null;

			if (aoHistPriceList.get(0) != null
					&& aoHistPriceList.get(33) != null) {
				double cp = aoHistPriceList.get(0).getClose();
				double cp34 = aoHistPriceList.get(33).getClose();

				if (cp34 > 0)
					return ((cp / cp34) * 100.0);
			}

			return null;
		}

		private Double calculateAwesomeOscillator(String dataId, Date maxDate) {
			List<HistoricalPriceIO> aoHistPriceList = null;

			// if (maxDate == null)
			// aoHistPriceList = dao.selectHistoricalPricesDescLimit(dataId,
			// 34);
			// else
			aoHistPriceList = dao.selectHistoricalPricesDescLimit(dataId, 34,
					maxDate);

			if (aoHistPriceList.size() < 34)
				return null;

			double sumHi34 = 0;
			double sumLo34 = 0;
			double sumHi5 = 0;
			double sumLo5 = 0;

			for (int i = 0; i < aoHistPriceList.size(); i++) {
				HistoricalPriceIO hpio = aoHistPriceList.get(i);
				sumHi34 = sumHi34 + hpio.getHigh();
				sumLo34 = sumLo34 + hpio.getLow();
				if (i < 5) {
					sumHi5 = sumHi5 + hpio.getHigh();
					sumLo5 = sumLo5 + hpio.getLow();
				}
			}
			double avgHi34 = sumHi34 / 34.0;
			double avgLo34 = sumLo34 / 34.0;
			double avgHi5 = sumHi5 / 5.0;
			double avgLo5 = sumLo5 / 5.0;

			return (avgHi5 + avgLo5 - avgHi34 - avgLo34) / 2.0;

		}

		// private double calculateSlope(String dataId, int dataPoints) {
		// // 1. get specific number of historical prices sorted desc
		// // 2. Use LeastSquares to calculate slope
		// // 3. save
		// if (histPriceList == null || histPriceList.size() < dataPoints) {
		// histPriceList = dao.selectHistoricalPricesDescLimit(dataId,
		// dataPoints);
		// if (histPriceList.size() < dataPoints)
		// return 0;
		// } else {
		// histPriceList = histPriceList.subList(0, dataPoints);
		// }
		//
		// Vector<Double> v = new Vector<Double>();
		// for (HistoricalPriceIO hp : histPriceList) {
		// v.add(hp.getClose());
		// }
		// Collections.reverse(v);
		//
		// LeastSquares ls = new LeastSquares(v, 1);
		// return (Double.valueOf(ls.getCoefficient(1)));
		// }

		private double calculateSlope(String dataId, int dataPoints,
				Date maxDate) {
			// 1. get specific number of historical prices sorted desc
			// 2. Use LeastSquares to calculate slope
			// 3. save
			if (histPriceList == null || histPriceList.size() < dataPoints) {
				// if (maxDate == null)
				// histPriceList = dao.selectHistoricalPricesDescLimit(dataId,
				// dataPoints);
				// else
				histPriceList = dao.selectHistoricalPricesDescLimit(dataId,
						dataPoints, maxDate);

				if (histPriceList.size() < dataPoints)
					return 0;
			} else {
				histPriceList = histPriceList.subList(0, dataPoints);
			}

			Vector<Double> v = new Vector<Double>();
			for (HistoricalPriceIO hp : histPriceList) {
				v.add(hp.getClose());
			}
			Collections.reverse(v);

			LeastSquares ls = new LeastSquares(v, 1);
			return (Double.valueOf(ls.getCoefficient(1)));
		}

		private double calculateSMA(String dataId, int dataPoints, Date maxDate) {
			if (histPriceList == null || histPriceList.size() < dataPoints) {
				histPriceList = dao.selectHistoricalPricesDescLimit(dataId,
						dataPoints, maxDate);

				if (histPriceList.size() < dataPoints)
					return 0;
			} else {
				histPriceList = histPriceList.subList(0, dataPoints);
			}

			double sum = 0;
			for (HistoricalPriceIO hp : histPriceList)
				sum += hp.getClose();

			return sum / dataPoints;
		}
	}

}
