public class LSTM {
	
	public static class LSTMnWeights {
		private final int iNs, oNs, hNs, hLs;
		
		double[][] Wix;
		double[][][] Wim;
		double[][] Wic;
		double[][] Wfx;
		double[][][] Wfm;
		double[][] Wfc;
		double[][] Wcx;
		double[][][] Wcm;
		double[][] Wox;
		double[][][] Wom;
		double[][] Woc;
		double[][][] Wll;
		double[][] Wym;
		
		public LSTMnWeights(int inputNvectSize, int outputNvectSize, int hiddenNs, int hiddenLayers) {
			this.iNs = inputNvectSize;
			this.oNs = outputNvectSize;
			this.hNs = hiddenNs;
			this.hLs = hiddenLayers;
			
			Wix = new double[hiddenNs][inputNvectSize];
			Wim = new double[hiddenLayers][hiddenNs][hiddenNs];
			Wic = new double[hiddenLayers][hiddenNs];
			Wfx = new double[hiddenNs][inputNvectSize];
			Wfm = new double[hiddenLayers][hiddenNs][hiddenNs];
			Wfc = new double[hiddenLayers][hiddenNs];
			Wcx = new double[hiddenNs][inputNvectSize];
			Wcm = new double[hiddenLayers][hiddenNs][hiddenNs];
			Wox = new double[hiddenNs][inputNvectSize];
			Wom = new double[hiddenLayers][hiddenNs][hiddenNs];
			Woc = new double[hiddenLayers][hiddenNs];
			Wll = new double[hiddenLayers-1][hiddenNs][hiddenNs];
			Wym = new double[outputNvectSize][hiddenNs];
		}
		
		public LSTMnWeights(LSTMnWeights old) {
			this.iNs = new Integer(old.iNs);
			this.oNs = new Integer(old.oNs);
			this.hNs = new Integer(old.hNs);
			this.hLs = new Integer(old.hLs);
			
			Wix = new double[hNs][0];
			Wim = new double[hLs][hNs][0];
			Wic = new double[hLs][0];
			Wfx = new double[hNs][0];
			Wfm = new double[hLs][hNs][0];
			Wfc = new double[hLs][0];
			Wcx = new double[hNs][0];
			Wcm = new double[hLs][hNs][0];
			Wox = new double[hNs][0];
			Wom = new double[hLs][hNs][0];
			Woc = new double[hLs][0];
			Wll = new double[hLs-1][hNs][0];
			Wym = new double[oNs][0];
			
			for (int i=0;i<hNs;i++) {
				this.Wix[i] = old.Wix[i].clone();
				this.Wfx[i] = old.Wfx[i].clone();
				this.Wcx[i] = old.Wcx[i].clone();
				this.Wox[i] = old.Wox[i].clone();
			}
			for (int i=0;i<hLs;i++)
				for (int j=0;j<hNs;j++) {
					this.Wim[i][j] = old.Wim[i][j].clone();
					this.Wfm[i][j] = old.Wfm[i][j].clone();
					this.Wcm[i][j] = old.Wcm[i][j].clone();
					this.Wom[i][j] = old.Wom[i][j].clone();
				}
			for (int i=0;i<hLs;i++) {
				this.Wic[i] = old.Wic[i].clone();
				this.Wfc[i] = old.Wfc[i].clone();
				this.Woc[i] = old.Woc[i].clone();
			}
			for (int i=0;i<hLs-1;i++)
				for (int j=0;j<hNs;j++) {
					this.Wll[i][j] = old.Wll[i][j].clone();
				}
			for (int i=0;i<oNs;i++) {
				this.Wym[i] = old.Wym[i].clone();
			}
		}
		
		public void randomWeights() {
			for (int i=0;i<Wix.length;i++)
				for (int j=0;j<Wix[i].length;j++) {
					Wix[i][j] = Math.random()*2-1;
					Wfx[i][j] = Math.random()*2-1;
					Wcx[i][j] = Math.random()*2-1;
					Wox[i][j] = Math.random()*2-1;
				}											
			for (int i=0;i<Wim.length;i++)
				for (int j=0;j<Wim[i].length;j++)
					for (int k=0;k<Wim[i][j].length;k++) {
						Wim[i][j][k] = Math.random()*2-1;
						Wfm[i][j][k] = Math.random()*2-1;
						Wcm[i][j][k] = Math.random()*2-1;
						Wom[i][j][k] = Math.random()*2-1;
					}
			for (int i=0;i<Wic.length;i++)
				for (int j=0;j<Wic[i].length;j++) {
					Wic[i][j] = Math.random()*2-1;
					Wfc[i][j] = Math.random()*2-1;
					Woc[i][j] = Math.random()*2-1;
				}
			for (int i=0;i<Wll.length;i++)
				for (int j=0;j<Wll[i].length;j++)
					for (int k=0;k<Wll[i][j].length;k++) {
						Wll[i][j][k] = (Math.random()*2-1)/hNs;
					}
			for (int i=0;i<Wym.length;i++)
				for (int j=0;j<Wym[i].length;j++) {
					Wym[i][j] = Math.random()*2-1;
				}
		}
		
		public void mutateWeights(int absN, double maxmutatepercent) {
			for (int i=0;i<absN;i++) {
				int r;
				if (hLs>1)
					r = (int)(Math.random()*13);
				else
					r = (int)(Math.random()*12);
					
				int r1, r2, r3;
				double mx = (Math.random()*2-1)*(maxmutatepercent/100);
				switch (r) {
					case 0:
						r1 = (int)(Math.random()*Wix.length);
						r2 = (int)(Math.random()*Wix[r1].length);
						Wix[r1][r2] = Wix[r1][r2]+mx;
						break;
					case 1:
						r1 = (int)(Math.random()*Wim.length);
						r2 = (int)(Math.random()*Wim[r1].length);
						r3 = (int)(Math.random()*Wim[r1][r2].length);
						Wim[r1][r2][r3] = Wim[r1][r2][r3]+mx;
						break;
					case 2:
						r1 = (int)(Math.random()*Wic.length);
						r2 = (int)(Math.random()*Wic[r1].length);
						Wic[r1][r2] = Wic[r1][r2]+mx;
						break;
					case 3:
						r1 = (int)(Math.random()*Wfx.length);
						r2 = (int)(Math.random()*Wfx[r1].length);
						Wfx[r1][r2] = Wfx[r1][r2]+mx;
						break;
					case 4:
						r1 = (int)(Math.random()*Wfm.length);
						r2 = (int)(Math.random()*Wfm[r1].length);
						r3 = (int)(Math.random()*Wfm[r1][r2].length);
						Wfm[r1][r2][r3] = Wfm[r1][r2][r3]+mx;
						break;
					case 5:
						r1 = (int)(Math.random()*Wfc.length);
						r2 = (int)(Math.random()*Wfc[r1].length);
						Wfc[r1][r2] = Wfc[r1][r2]+mx;
						break;
					case 6:
						r1 = (int)(Math.random()*Wcx.length);
						r2 = (int)(Math.random()*Wcx[r1].length);
						Wcx[r1][r2] = Wcx[r1][r2]+mx;
						break;
					case 7:
						r1 = (int)(Math.random()*Wcm.length);
						r2 = (int)(Math.random()*Wcm[r1].length);
						r3 = (int)(Math.random()*Wcm[r1][r2].length);
						Wcm[r1][r2][r3] = Wcm[r1][r2][r3]+mx;
						break;
					case 8:
						r1 = (int)(Math.random()*Wox.length);
						r2 = (int)(Math.random()*Wox[r1].length);
						Wox[r1][r2] = Wox[r1][r2]+mx;
						break;
					case 9:
						r1 = (int)(Math.random()*Wom.length);
						r2 = (int)(Math.random()*Wom[r1].length);
						r3 = (int)(Math.random()*Wom[r1][r2].length);
						Wom[r1][r2][r3] = Wom[r1][r2][r3]+mx;
						break;
					case 10:
						r1 = (int)(Math.random()*Woc.length);
						r2 = (int)(Math.random()*Woc[r1].length);
						Woc[r1][r2] = Woc[r1][r2]+mx;
						break;
					case 11:
						r1 = (int)(Math.random()*Wym.length);
						r2 = (int)(Math.random()*Wym[r1].length);
						Wym[r1][r2] = Wym[r1][r2]+mx;
						break;
					case 12:
						r1 = (int)(Math.random()*Wll.length);
						r2 = (int)(Math.random()*Wll[r1].length);
						r3 = (int)(Math.random()*Wll[r1][r2].length);
						Wll[r1][r2][r3] = Wll[r1][r2][r3]+mx;
						break;
				}
			}
		}
		
	}
	
	public static class LSTMnetwork {
		private final int iNs, oNs, hNs, hLs;
		
		public double[] input;
		private double[] output;
		private double[][] ctm1;
		private double[][] ct;
		private double[][] mtm1;
		private double[][] mt;
		LSTMnWeights ws;
		
		public LSTMnetwork(int inputNvectSize, int outputNvectSize, int hiddenNs, int hiddenLayers) {
			this.iNs = inputNvectSize;
			this.oNs = outputNvectSize;
			this.hNs = hiddenNs;
			this.hLs = hiddenLayers;
			
			input = new double[inputNvectSize];
			output = new double[outputNvectSize];
			ctm1 = new double[hiddenLayers][hiddenNs];
			ct = new double[hiddenLayers][hiddenNs];
			mtm1 = new double[hiddenLayers][hiddenNs];
			mt = new double[hiddenLayers][hiddenNs];
			this.ws = new LSTMnWeights(iNs, oNs, hNs, hLs);
		}
		
		public void resetStates() {
			ctm1 = new double[hLs][hNs];
			mtm1 = new double[hLs][hNs];
		}
		
		private double sigma(double x) {
			return 1 / (1 + Math.exp(-x) );
		}
		
		private double tanh(double x) {
			return (Math.exp(2*x)-1)/(Math.exp(2*x)+1);
		}
		
		private double scalarM(double[] a, double[] b) {
			double summ = 0;
			for (int i=0;i<a.length;i++) {
				summ += a[i]*b[i];
			}
			return summ;
		}
		
		public double[] runNeural() {
			for (int i=0;i<hNs;i++) {
				double it = sigma( scalarM(input, ws.Wix[i]) + scalarM(mtm1[0], ws.Wim[0][i]) + ctm1[0][i]*ws.Wic[0][i] );
				double ft = sigma( scalarM(input, ws.Wfx[i]) + scalarM(mtm1[0], ws.Wfm[0][i]) + ctm1[0][i]*ws.Wfc[0][i] );
				ct[0][i] = ft*ctm1[0][i] + it*tanh( scalarM(input, ws.Wcx[i]) + scalarM(mtm1[0], ws.Wcm[0][i]) );
				double ot = sigma( scalarM(input, ws.Wox[i]) + scalarM(mtm1[0], ws.Wom[0][i]) + ct[0][i]*ws.Woc[0][i] );
				mt[0][i] = ot * tanh(ct[0][i]);
			}
			
			for (int k=1;k<hLs;k++)
				for (int i=0;i<hNs;i++) {
					double it = sigma( scalarM(mt[k-1], ws.Wll[k-1][i]) + scalarM(mtm1[k], ws.Wim[k][i]) + ctm1[k][i]*ws.Wic[k][i] );
					double ft = sigma( scalarM(mt[k-1], ws.Wll[k-1][i]) + scalarM(mtm1[k], ws.Wfm[k][i]) + ctm1[k][i]*ws.Wfc[k][i] );
					ct[k][i] = ft*ctm1[k][i] + it*tanh( scalarM(mt[k-1], ws.Wll[k-1][i]) + scalarM(mtm1[k], ws.Wcm[k][i]) );
					double ot = sigma( scalarM(mt[k-1], ws.Wll[k-1][i]) + scalarM(mtm1[k], ws.Wom[k][i]) + ct[k][i]*ws.Woc[k][i] );
					mt[k][i] = ot * tanh(ct[k][i]);
				}
			
			double outputsumm = 0;			
			for (int i=0;i<oNs;i++) {
				output[i] = sigma( scalarM(mt[hLs-1], ws.Wym[i]) );
				outputsumm += output[i];
			}
			for (int i=0;i<oNs;i++)
				output[i] = output[i] / outputsumm;
						
			ctm1 = ct;
			mtm1 = mt;
			ct = new double[hLs][hNs];
			mt = new double[hLs][hNs];
			
			return output;
		}
	}
	
	public static void test2() {
		int[] testseq = {1, 3, 2, 4, 4, 0, 1, 2, 3, 0};
		int vc = 5;
		
		LSTMnetwork ann = new LSTMnetwork(vc, vc, 5, 1);		
		
		double[] out;
		double preverr=0;
		double err=0;
		
		ann.ws.randomWeights();
		LSTMnWeights oldws = new LSTMnWeights(ann.ws);
		for (int i=0;i<testseq.length*4;i++) {
			for (int j=0;j<vc;j++) {
				if (j==testseq[i % testseq.length])
					ann.input[j] = 1;
				else
					ann.input[j] = 0;
			}
			out=ann.runNeural();
			for (int k=0;k<vc;k++) {
				if (testseq[((i+1) % testseq.length)]==k)
					preverr += Math.abs(1-out[k]);
				else
					preverr += Math.abs(out[k]);
			}
		}
		
		int count = 0;
		while (true) {
			err=0;
			ann.resetStates();
			if (preverr>40) {
				ann.ws.mutateWeights(20, 10d);
			} else if (preverr>30) {
				ann.ws.mutateWeights(10, 10d);
			} else if (preverr>20) {
				ann.ws.mutateWeights(10, 5d);
			} else if (preverr>10) {
				ann.ws.mutateWeights(5, 5d);
			} else if (preverr>5) {
				ann.ws.mutateWeights(5, 2d);
			} else {
				ann.ws.mutateWeights(2, 2d);
			}
			//ann.mutateWeights(20, 5d);
			
			for (int i=0;i<testseq.length*4;i++) {
				for (int j=0;j<vc;j++) {
					if (j==testseq[i % testseq.length])
						ann.input[j] = 1;
					else
						ann.input[j] = 0;
				}
				out=ann.runNeural();
				
				double max=-1.0E5f;
				for (double o : out) {
					max=Math.max(max, o);
				}
				int winner=-1;
				for (int k=0;k<vc;k++)
					if (max==out[k]) {
						winner=k;
						break;
					}
				System.out.print(winner + ", ");
				
				for (int k=0;k<vc;k++) {
					if (testseq[((i+1) % testseq.length)]==k) {
						err += Math.abs(1-out[k]);
					} else {
						err += Math.abs(out[k]);
					}						
					//System.out.print(out[k] + ", ");
				}
				//System.out.println(" ");
			}
			System.out.println(" ");
			
			if (err<preverr) {
				oldws = new LSTMnWeights(ann.ws);
				preverr=err;
				System.out.println("Good " + err + " < " + preverr + " c:" + count);
			} else {
				ann.ws = new LSTMnWeights(oldws);
				System.out.println("Bad " + err + " > " + preverr + " c:" + count);
			}
			
			count++;
		}
	}
	
}
