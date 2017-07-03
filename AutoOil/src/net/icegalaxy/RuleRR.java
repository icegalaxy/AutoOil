package net.icegalaxy;

//Use the OPEN Line

public class RuleRR extends Rules
{

	OHLC currentOHLC;

	public RuleRR(boolean globalRunRule)
	{
		super(globalRunRule);
		setOrderTime(60000, 235900, 235900, 235900, 235900, 235900);
		// wait for EMA6, that's why 0945
	}

	public void openContract()
	{
		
		// in case thread not ready
		try{
			getTimeBase().getLatestCandle().getClose();
			GetData.getLongTB().getEma5().getEMA();
		}catch (Exception e)
		{
			sleep(5000);
			return;
		}
		

		if (!isOrderTime() || Global.getNoOfContracts() != 0 || Global.balance < -0.3)
			return;

		for (OHLC item : XMLWatcher.ohlcs)
		{
			currentOHLC = item;
//			setOrderTime(item.getOrderTime());

			if (Global.getNoOfContracts() != 0)
				return;

			if (currentOHLC.cutLoss == 0)
				continue;

			if (currentOHLC.shutdown)
				continue;

			if (GetData.getLongTB().getEma5().getEMA() > currentOHLC.cutLoss && Global.getCurrentPoint() < currentOHLC.cutLoss + 0.1
					&& Global.getCurrentPoint() > currentOHLC.cutLoss)
			{

				Global.addLog("Reached " + currentOHLC.name);
				
				while (Global.isRapidDrop()
						|| getTimeBase().getLatestCandle().getOpen() > getTimeBase().getLatestCandle().getClose())
				{

					if (GetData.getLongTB().getEma5().getEMA() < currentOHLC.cutLoss)
					{
						Global.addLog("EMA5 out of range");
						return;
					}

					if (Global.getCurrentPoint() < currentOHLC.cutLoss - 0.1)
					{
						Global.addLog("Current point out of range");
						return;
					}

					sleep(1000);
				}

				longContract();
				Global.addLog("OHLC: " + currentOHLC.name);
				return;

			} else if (GetData.getLongTB().getEma5().getEMA() < currentOHLC.cutLoss
					&& Global.getCurrentPoint() > currentOHLC.cutLoss - 0.1
					&& Global.getCurrentPoint() < currentOHLC.cutLoss)
			{
				
				Global.addLog("Reached " + currentOHLC.name);

				while (Global.isRapidRise()
						|| getTimeBase().getLatestCandle().getOpen() < getTimeBase().getLatestCandle().getClose())
				{

					if (GetData.getLongTB().getEma5().getEMA() > currentOHLC.cutLoss)
					{
						Global.addLog("EMA5 out of range");
						return;
					}

					if (Global.getCurrentPoint() > currentOHLC.cutLoss + 0.1)
					{
						Global.addLog("Current point out of range");
						return;
					}

					sleep(1000);
				}

				shortContract();
				Global.addLog("OHLC: " + currentOHLC.name);
				return;

			}

		}
	}

	// use 1min instead of 5min
	double getCutLossPt()
	{

		if (Global.getNoOfContracts() > 0)
			return Math.max(0.2, buyingPoint - currentOHLC.cutLoss + 0.1);
		else
			return Math.max(0.2, currentOHLC.cutLoss - buyingPoint + 0.1);
	}

	@Override
	void stopEarn()
	{
		if (Global.getNoOfContracts() > 0)
		{

			if (Global.getCurrentPoint() < buyingPoint + 0.05)
				closeContract(className + ": Break even, short @ " + Global.getCurrentBid());
			else if (GetData.getShortTB().getLatestCandle().getClose() < tempCutLoss)
				closeContract(className + ": StopEarn, short @ " + Global.getCurrentBid());
			

		} else if (Global.getNoOfContracts() < 0)
		{

			if (Global.getCurrentPoint() > buyingPoint - 0.05)
				closeContract(className + ": Break even, long @ " + Global.getCurrentAsk());
			else if (GetData.getShortTB().getLatestCandle().getClose() > tempCutLoss)
				closeContract(className + ": StopEarn, long @ " + Global.getCurrentAsk());
			
		}
	}
	
	// @Override
	// protected void cutLoss()
	// {
	//
	// if (Global.getNoOfContracts() > 0)
	// {
	//
	// //breakEven
	// if (getProfit() > 20 && tempCutLoss < buyingPoint + 5)
	// tempCutLoss = buyingPoint + 5;
	//
	// if (Global.getCurrentPoint() < tempCutLoss)
	// {
	// closeContract(className + ": CutLoss, short @ " +
	// Global.getCurrentBid());
	// shutdown = true;
	// }
	// } else if (Global.getNoOfContracts() < 0)
	// {
	//
	// //breakEven
	// if (getProfit() > 20 && tempCutLoss > buyingPoint - 5)
	// tempCutLoss = buyingPoint - 5;
	//
	// if (Global.getCurrentPoint() > tempCutLoss)
	// {
	// closeContract(className + ": CutLoss, long @ " + Global.getCurrentAsk());
	// shutdown = true;
	// }
	//
	// }
	//
	// }

	// @Override
	// boolean trendReversed()
	// {
	// if (reverse == 0)
	// return false;
	// else if (Global.getNoOfContracts() > 0)
	// return Global.getCurrentPoint() < reverse;
	// else
	// return Global.getCurrentPoint() > reverse;
	// }

	double getStopEarnPt()
	{

		double intraDayStopEarn = XMLWatcher.stopEarn;

		if (intraDayStopEarn == 0)
		{
			if (Global.getNoOfContracts() > 0)
				return Math.max(0.1, currentOHLC.stopEarn - buyingPoint - 0.1);
			else
				return Math.max(0.1, buyingPoint - currentOHLC.stopEarn - 0.1);
		} else
		{
			if (Global.getNoOfContracts() > 0)
				return Math.max(0.1, intraDayStopEarn - buyingPoint - 0.1);
			else
				return Math.max(0.1, buyingPoint - intraDayStopEarn - 0.1);

		}
	}

	// @Override
	// public void trendReversedAction()
	// {
	//
	// trendReversed = true;
	// }

	@Override
	public TimeBase getTimeBase()
	{
		return GetData.getLongTB();
	}
}