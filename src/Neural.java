import com.amd.aparapi.device.Device;
import com.amd.aparapi.Range;
import com.amd.aparapi.Kernel;


public class Neural {		
	public static class SSVNkernel extends Kernel {
		private final Device device;
		
		private final int[] dataint;
		
		public final float[] input;
		private final float[] output;
		private final float[] t1;
		private final float[] t2;
		private final float[] Ws;
		
		private final Range range1;
		private final Range range2;
		
		public SSVNkernel(int inputNs, int outputNs, int hiddenNlayers, int hiddenNs) {
			dataint = new int[9];
			dataint[0] = inputNs;
			dataint[1] = outputNs;
			dataint[2] = hiddenNlayers;
			dataint[3] = hiddenNs;
			dataint[4] = inputNs*hiddenNs;
			dataint[5] = hiddenNs*hiddenNs;
			dataint[6] = inputNs*hiddenNs + (hiddenNlayers-1)*hiddenNs*hiddenNs;
			dataint[7] = 0;
			
			device = Device.firstGPU();
			range1 = device.createRange(hiddenNs);
			range2 = device.createRange(outputNs);
			this.setExplicit(true);
			
			input = new float[inputNs];
			output = new float[outputNs];
			t1 = new float[hiddenNs];
			t2 = new float[hiddenNs];
			if (hiddenNlayers % 2 == 0)
				dataint[8]=1;
			else
				dataint[8]=0;
			Ws = new float[inputNs*hiddenNs + (hiddenNlayers-1)*hiddenNs*hiddenNs + hiddenNs*outputNs];			
		}
		
		public SSVNkernel(SSVNkernel oldkernel, Device newdevice) {
			device = newdevice;
			dataint = oldkernel.dataint;
			input = oldkernel.input;
			output = oldkernel.output;
			t1 = oldkernel.t1;
			t2 = oldkernel.t2;
			Ws = oldkernel.Ws;
			
			range1 = device.createRange(dataint[3]);
			range2 = device.createRange(dataint[1]);
			
			oldkernel.dispose();
		}
		
		public void randomWeights() {
			for (int i=0; i<Ws.length; i++) {
				Ws[i] = (float)(Math.random()*2-1);
			}
		}
		
		public void mutateWeights(int Ntomutate, float maxmutpercent) {
			for (int i=0; i<Ntomutate; i++) {
				int index=(int)(Math.random()*Ws.length);
				Ws[index] = (float)(Ws[index]*(Math.random()*2-1)*(maxmutpercent/100) );
			}
		}
		
		public void mutateWeights(float Ntomutpercent, float maxmutpercent) {
			for (int i=0; i<(int)(Ws.length*Ntomutpercent/100); i++) {
				int index=(int)(Math.random()*Ws.length);
				Ws[index] = (float)(Ws[index]*(Math.random()*2-1)*(maxmutpercent/100) );
			}
		}
		
		public void writeWeightstoFile() {
			DataC.writeNeuralWeightsFile(Ws);
		}
		
		public void readWeightsfromFile() {
			float[] red = DataC.readNeuralWeightsFile();
			if (red.length==Ws.length)
				System.arraycopy(red, 0, Ws, 0, red.length);
			else
				Trader.addLogEntry("Couldn't read Neural Weights (May be uncompatible networks)");
		}
		
		public float[] runNeural() {
			dataint[7]=0;
			this.execute(range1);
			dataint[7]=1;
			this.execute(range1, dataint[2]-1);
			dataint[7]=2;
			this.execute(range2);
			
			return output;
		}
		
		@Override
		public void run() {
			float wSumm = 0;
			if (dataint[7]==1) {
				if (getPassId() % 2 == 0) {
					for (int i=0; i<dataint[3]; i++) {
						wSumm+=t1[i]*Ws[dataint[4] + getPassId()*dataint[5] + getGlobalId()*dataint[3] + i];
					}
					t2[getGlobalId()]=1 / (1 + exp(-wSumm) );
				} else {
					for (int i=0; i<dataint[3]; i++) {
						wSumm+=t2[i]*Ws[dataint[4] + getPassId()*dataint[5] + getGlobalId()*dataint[3] + i];
					}
					t1[getGlobalId()]=1 / (1 + exp(-wSumm) );
				}							
			} else if (dataint[7]==0) {
				for (int i=0; i<dataint[0]; i++) {
					wSumm+=input[i]*Ws[getGlobalId()*dataint[0] + i];
				}
				t1[getGlobalId()]=1 / (1 + exp(-wSumm) );
			} else {				
				if (dataint[8]==1) {
					for (int i=0; i<dataint[3]; i++) {
						wSumm+=t2[i]*Ws[dataint[6] + getGlobalId()*dataint[3] + i];
					}
					output[getGlobalId()]=1 / (1 + exp(-wSumm) );
				} else {
					for (int i=0; i<dataint[3]; i++) {
						wSumm+=t1[i]*Ws[dataint[6] + getGlobalId()*dataint[3] + i];
					}
					output[getGlobalId()]=1 / (1 + exp(-wSumm) );
				}
			}
			
		}
	}
	
	public static class LSTMkernel extends Kernel {
		private final Device device;
		private final Range range1;
		private final Range range2;
		
		private final int[] dataint;
		
		public final float[] input;
		private final float[] output;
		
		private final float[] it;
		private final float[] ft;
		private final float[] ct1;
		private final float[] ct2;
		private final float[] ot;
		private final float[] ht1;
		private final float[] ht2;
		
		private final float[][] Wxi, OWxi;
		private final float[][] Whi, OWhi;
		private final float[] Wci, OWci;
		private final float[] bi, Obi;
		private final float[][] Wxf, OWxf;
		private final float[][] Whf, OWhf;
		private final float[] Wcf, OWcf;
		private final float[] bf, Obf;
		private final float[][] Wxc, OWxc;
		private final float[][] Whc, OWhc;
		private final float[] bc, Obc;
		private final float[][] Wxo, OWxo;
		private final float[][] Who, OWho;
		private final float[] Wco, OWco;
		private final float[] bo, Obo;
		private final float[][] Why, OWhy;
		private final float[] by, Oby;
		
		public LSTMkernel(int inputNvectSize, int outputNvectSize, int hiddenNs) {
			device = Device.firstGPU();
			range1 = device.createRange(hiddenNs);
			range2 = device.createRange(outputNvectSize);
			this.setExplicit(true);
			
			dataint = new int[5];
			dataint[0] = inputNvectSize;
			dataint[1] = outputNvectSize;
			dataint[2] = hiddenNs;
			dataint[3] = 0;
			dataint[4] = 0;
			
			input = new float[inputNvectSize];
			output = new float[outputNvectSize];
			
			it = new float[hiddenNs];
			ft = new float[hiddenNs];
			ct1 = new float[hiddenNs];
			ct2 = new float[hiddenNs];
			ot = new float[hiddenNs];
			ht1 = new float[hiddenNs];
			ht2 = new float[hiddenNs];
			
			Wxi = new float[hiddenNs][inputNvectSize];
			Whi = new float[hiddenNs][hiddenNs];
			Wci = new float[hiddenNs];
			bi = new float[hiddenNs];
			Wxf = new float[hiddenNs][inputNvectSize];
			Whf = new float[hiddenNs][hiddenNs];
			Wcf = new float[hiddenNs];
			bf = new float[hiddenNs];
			Wxc = new float[hiddenNs][inputNvectSize];
			Whc = new float[hiddenNs][hiddenNs];
			bc = new float[hiddenNs];
			Wxo = new float[hiddenNs][inputNvectSize];		
			Who = new float[hiddenNs][hiddenNs];
			Wco = new float[hiddenNs];
			bo = new float[hiddenNs];
			Why = new float[outputNvectSize][hiddenNs];
			by = new float[outputNvectSize];
			
			OWxi = new float[hiddenNs][inputNvectSize];
			OWhi = new float[hiddenNs][hiddenNs];
			OWci = new float[hiddenNs];
			Obi = new float[hiddenNs];
			OWxf = new float[hiddenNs][inputNvectSize];
			OWhf = new float[hiddenNs][hiddenNs];
			OWcf = new float[hiddenNs];
			Obf = new float[hiddenNs];
			OWxc = new float[hiddenNs][inputNvectSize];
			OWhc = new float[hiddenNs][hiddenNs];
			Obc = new float[hiddenNs];
			OWxo = new float[hiddenNs][inputNvectSize];		
			OWho = new float[hiddenNs][hiddenNs];
			OWco = new float[hiddenNs];
			Obo = new float[hiddenNs];
			OWhy = new float[outputNvectSize][hiddenNs];
			Oby = new float[outputNvectSize];
		}
		
		public void randomWeights() {
			for (int i=0; i<dataint[2]; i++)
				for (int j=0; j<dataint[0]; j++) {
					Wxi[i][j] = (float)(Math.random()*2-1);
					Wxf[i][j] = (float)(Math.random()*2-1);
					Wxc[i][j] = (float)(Math.random()*2-1);
					Wxo[i][j] = (float)(Math.random()*2-1);
				}
			for (int i=0; i<dataint[2]; i++)
				for (int j=0; j<dataint[2]; j++) {
					Whi[i][j] = (float)(Math.random()*2-1);
					Whf[i][j] = (float)(Math.random()*2-1);					
					Whc[i][j] = (float)(Math.random()*2-1);
					Who[i][j] = (float)(Math.random()*2-1);
				}
			
			for (int i=0; i<dataint[2]; i++) {
				Wci[i] = (float)(Math.random()*2-1);
				//bi[i] = (float)(Math.random()-0.5);
				Wcf[i] = (float)(Math.random()*2-1);
				//bf[i] = (float)(Math.random() );
				//bc[i] = 0f;
				Wco[i] = (float)(Math.random()*2-1);
				//bo[i] = (float)(Math.random()-0.5);	
			}
			
			for (int i=0; i<dataint[1]; i++)
				for (int j=0; j<dataint[2]; j++) {
					Why[i][j] = (float)(Math.random()*2-1);
				}
			
			/*for (int i=0; i<dataint[1]; i++) {
				by[i] = 0f;
			}*/
		}
		
		public void saveWeights() {
			for (int i=0;i<dataint[2];i++) {
				System.arraycopy(Wxi[i], 0, OWxi[i], 0, dataint[0]);
				System.arraycopy(Wxf[i], 0, OWxf[i], 0, dataint[0]);
				System.arraycopy(Wxc[i], 0, OWxc[i], 0, dataint[0]);
				System.arraycopy(Wxo[i], 0, OWxo[i], 0, dataint[0]);
			}
			for (int i=0; i<dataint[2]; i++) {
				System.arraycopy(Whi[i], 0, OWhi[i], 0, dataint[2]);				
				System.arraycopy(Whf[i], 0, OWhf[i], 0, dataint[2]);			
				System.arraycopy(Whc[i], 0, OWhc[i], 0, dataint[2]);
				System.arraycopy(Who[i], 0, OWho[i], 0, dataint[2]);			
			}
			System.arraycopy(Wci, 0, OWci, 0, dataint[2]);
			System.arraycopy(bi, 0, Obi, 0, dataint[2]);
			System.arraycopy(Wcf, 0, OWcf, 0, dataint[2]);
			System.arraycopy(bf, 0, Obf, 0, dataint[2]);
			System.arraycopy(bc, 0, Obc, 0, dataint[2]);
			System.arraycopy(Wco, 0, OWco, 0, dataint[2]);
			System.arraycopy(bo, 0, Obo, 0, dataint[2]);
			for (int i=0; i<dataint[1]; i++) {
				System.arraycopy(Why[i], 0, OWhy[i], 0, dataint[2]);
			}
			System.arraycopy(by, 0, Oby, 0, dataint[1]);
		}
		
		public void loadSavedWeights() {
			for (int i=0;i<dataint[2];i++) {
				System.arraycopy(OWxi[i], 0, Wxi[i], 0, dataint[0]);
				System.arraycopy(OWxf[i], 0, Wxf[i], 0, dataint[0]);
				System.arraycopy(OWxc[i], 0, Wxc[i], 0, dataint[0]);
				System.arraycopy(OWxo[i], 0, Wxo[i], 0, dataint[0]);
			}
			for (int i=0; i<dataint[2]; i++) {
				System.arraycopy(OWhi[i], 0, Whi[i], 0, dataint[2]);
				System.arraycopy(OWhf[i], 0, Whf[i], 0, dataint[2]);				
				System.arraycopy(OWhc[i], 0, Whc[i], 0, dataint[2]);
				System.arraycopy(OWho[i], 0, Who[i], 0, dataint[2]);			
			}
			System.arraycopy(OWci, 0, Wci, 0, dataint[2]);
			System.arraycopy(Obi, 0, bi, 0, dataint[2]);
			System.arraycopy(OWcf, 0, Wcf, 0, dataint[2]);
			System.arraycopy(Obf, 0, bf, 0, dataint[2]);
			System.arraycopy(Obc, 0, bc, 0, dataint[2]);
			System.arraycopy(OWco, 0, Wco, 0, dataint[2]);
			System.arraycopy(Obo, 0, bo, 0, dataint[2]);
			for (int i=0; i<dataint[1]; i++) {
				System.arraycopy(OWhy[i], 0, Why[i], 0, dataint[2]);
			}
			System.arraycopy(Oby, 0, by, 0, dataint[1]);
		}
		
		public void mutateWeights(int absN, float maxmutatepercent) {
			for (int i=0;i<absN;i++) {
				int r = (int)(Math.random()*12);
				int r1, r2;
				float mx = (float)((Math.random()*2-1)*(maxmutatepercent/100));
				switch (r) {
					case 0:
						r1 = (int)(Math.random()*Wxi.length);
						r2 = (int)(Math.random()*Wxi[r1].length);
						Wxi[r1][r2] = Wxi[r1][r2]+mx;
					case 1:
						r1 = (int)(Math.random()*Whi.length);
						r2 = (int)(Math.random()*Whi[r1].length);
						Whi[r1][r2] = Whi[r1][r2]+mx;
					case 2:
						r1 = (int)(Math.random()*Wci.length);
						Wci[r1] = Wci[r1]+mx;
					case 3:
						r1 = (int)(Math.random()*Wxf.length);
						r2 = (int)(Math.random()*Wxf[r1].length);
						Wxf[r1][r2] = Wxf[r1][r2]+mx;
					case 4:
						r1 = (int)(Math.random()*Whf.length);
						r2 = (int)(Math.random()*Whf[r1].length);
						Whf[r1][r2] = Whf[r1][r2]+mx;
					case 5:
						r1 = (int)(Math.random()*Wcf.length);
						Wcf[r1] = Wcf[r1]+mx;
					case 6:
						r1 = (int)(Math.random()*Wxc.length);
						r2 = (int)(Math.random()*Wxc[r1].length);
						Wxc[r1][r2] = Wxc[r1][r2]+mx;
					case 7:
						r1 = (int)(Math.random()*Whc.length);
						r2 = (int)(Math.random()*Whc[r1].length);
						Whc[r1][r2] = Whc[r1][r2]+mx;
					case 8:
						r1 = (int)(Math.random()*Wxo.length);
						r2 = (int)(Math.random()*Wxo[r1].length);
						Wxo[r1][r2] = Wxo[r1][r2]+mx;
					case 9:
						r1 = (int)(Math.random()*Who.length);
						r2 = (int)(Math.random()*Who[r1].length);
						Who[r1][r2] = Who[r1][r2]+mx;
					case 10:
						r1 = (int)(Math.random()*Wco.length);
						Wco[r1] = Wco[r1]+mx;
					case 11:
						r1 = (int)(Math.random()*Why.length);
						r2 = (int)(Math.random()*Why[r1].length);
						Why[r1][r2] = Why[r1][r2]+mx;
					case 12:
						r1 = (int)(Math.random()*bi.length);
						bi[r1] = bi[r1]+mx;
					case 13:
						r1 = (int)(Math.random()*bf.length);
						bf[r1] = bf[r1]+mx;
					case 14:
						r1 = (int)(Math.random()*bo.length);
						bo[r1] = bo[r1]+mx;
					case 15:
						r1 = (int)(Math.random()*bc.length);
						bc[r1] = bc[r1]+mx;
					case 16:
						r1 = (int)(Math.random()*by.length);
						by[r1] = by[r1]+mx;
				}
			}
		}
		
		public void resetStates() {
			for (int i=0; i<dataint[2]; i++) {
				ct1[i] = 0f;
				ct2[i] = 0f;
				ht1[i] = 0f;
				ht2[i] = 0f;				
			}
		}
		
		public float[] runNeural() {
			if (dataint[3]==1)
				dataint[3]=2;
			else
				dataint[3]=1;
			
			dataint[4]=0;
			this.execute(range1);
			dataint[4]=1;
			this.execute(range2);
			
			float outputsumm = 0;
			for(int i=0;i<output.length;i++) {
				outputsumm += output[i];
			}
			for(int i=0;i<output.length;i++) {
				output[i] = output[i] / outputsumm;
			}
			
			return output;
		}
		
		
		private float sigma(float x) {
			return 1 / (1 + exp(-x) );
			//return (1 / (1 + exp(-x) ))*2-1;
		}
		
		private float tanh(float x) {
			return (exp(2*x)-1)/(exp(2*x)+1);
			//return ((exp(2*x)-1)/(exp(2*x)+1))*2-1;
		}
		
		private float scalarM(float[] a, float[] b, int length) {
			float summ = 0;
			for (int i=0;i<length;i++) {
				summ += a[i]*b[i];
			}
			return summ;
		}
		
		@Override
		public void run() {
			int gid = getGlobalId();
			int l1 = dataint[0];
			int l2 = dataint[2];
			
			if (dataint[4]==0) {
				if (dataint[3]==1) {
					it[gid] = sigma( scalarM(input, Wxi[gid], l1) + scalarM(ht1, Whi[gid], l2) + ct1[gid]*Wci[gid] + bi[gid] );
					ft[gid] = sigma( scalarM(input, Wxf[gid], l1) + scalarM(ht1, Whf[gid], l2) + ct1[gid]*Wcf[gid] + bf[gid] );
					ct2[gid] = ft[gid]*ct1[gid] + it[gid]*tanh( scalarM(input, Wxc[gid], l1) + scalarM(ht1, Whc[gid], l2) + bc[gid] );
					ot[gid] = sigma( scalarM(input, Wxo[gid], l1) + scalarM(ht1, Who[gid], l2) + ct2[gid]*Wco[gid] + bo[gid] );
					ht2[gid] = ot[gid] * tanh(ct2[gid]);
				} else {
					it[gid] = sigma( scalarM(input, Wxi[gid], l1) + scalarM(ht2, Whi[gid], l2) + ct2[gid]*Wci[gid] + bi[gid] );
					ft[gid] = sigma( scalarM(input, Wxf[gid], l1) + scalarM(ht2, Whf[gid], l2) + ct2[gid]*Wcf[gid] + bf[gid] );
					ct1[gid] = ft[gid]*ct2[gid] + it[gid]*tanh( scalarM(input, Wxc[gid], l1) + scalarM(ht2, Whc[gid], l2) + bc[gid] );
					ot[gid] = sigma( scalarM(input, Wxo[gid], l1) + scalarM(ht2, Who[gid], l2) + ct1[gid]*Wco[gid] + bo[gid] );
					ht1[gid] = ot[gid] * tanh(ct1[gid]);
				}
			} else {
				if (dataint[3]==1) {
					output[gid] = sigma( scalarM(ht2, Why[gid], l2) + by[gid] );
				} else {
					output[gid] = sigma( scalarM(ht1, Why[gid], l2) + by[gid] );
				}
			}
		}
		
	}
	
	public static void printarray(float[] a) {
		for (int i=0; i<a.length;i++) {
			System.out.print(a[i] + ", ");
		}
		System.out.println();
	}
	
	public static void test3() {
		LSTMkernel ann = new LSTMkernel(5, 5, 1000);
		float[] out;
		
		ann.randomWeights();
		
		int count = 0;
		while (true) {
			out=ann.runNeural();
			
			System.out.println(out[0] + ", c:" + count);
			count++;
		}
	}
	
	public static void test2() {
		int[] testseq = {1, 3, 2, 4, 4, 0, 1, 2, 3, 0};
		int vc = 5;
		
		LSTMkernel ann = new LSTMkernel(vc, vc, 30);
		
		float[] out;
		float preverr=0;
		float err=0;
		
		ann.randomWeights();
		ann.saveWeights();
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
				ann.mutateWeights(20, 10f);
			} else if (preverr>30) {
				ann.mutateWeights(10, 10f);
			} else if (preverr>20) {
				ann.mutateWeights(10, 5f);
			} else if (preverr>10) {
				ann.mutateWeights(5, 5f);
			} else if (preverr>5) {
				ann.mutateWeights(5, 2f);
			} else {
				ann.mutateWeights(2, 2f);
			}
			//ann.mutateWeights(20, 5f);
			
			for (int i=0;i<testseq.length*4;i++) {
				for (int j=0;j<vc;j++) {
					if (j==testseq[i % testseq.length])
						ann.input[j] = 1;
					else
						ann.input[j] = 0;
				}
				out=ann.runNeural();
				
				float max=-1.0E5f;
				for (float o : out) {
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
				ann.saveWeights();
				preverr=err;
				System.out.println("Good " + err + " < " + preverr + " c:" + count);
			} else {
				ann.loadSavedWeights();
				System.out.println("Bad " + err + " > " + preverr + " c:" + count);
			}
			
			count++;
		}
		
		/*long st=System.currentTimeMillis();
		for (int j=0; j<100; j++) {
			//ann.resetStates();
			out=ann.runNeural();
			System.out.println(out[0]);
		}
		long ct=System.currentTimeMillis();
		System.out.println(ct-st);*/
	}
	
	public static void test() {
		SSVNkernel ann = new SSVNkernel(10, 1, 10, 100);
		float[] prevout;
		float[] out;
		float[] prevWs;
		
		for (int i=0; i<ann.input.length; i++) {
			ann.input[i]=(float)(Math.random()*2-1);
		}
		ann.randomWeights();
		prevout=ann.runNeural().clone();
		System.out.println(prevout[0]);	
		
		//ann = new SSVNkernel(ann, Device.best());
		
		while (true) {
			prevWs = ann.Ws;
			ann.mutateWeights(1f, 1f);
			out=ann.runNeural();
			if (Math.abs(prevout[0]-0.5)>Math.abs(out[0]-0.5) ) {
				System.out.println(out[0]);
				prevWs = ann.Ws;
				prevout=out.clone();
			} else {
				System.arraycopy(prevWs, 0, ann.Ws, 0, prevWs.length);
			}
		}
		
		/*long st=System.currentTimeMillis();
		for (int j=0; j<10; j++) {
			for (int i=0; i<ann.input.length; i++) {
				ann.input[i]=(float)(Math.random()*2-1);
			}
			out=ann.runNeural();
			System.out.println(out[0]);
		}
		long ct=System.currentTimeMillis();
		System.out.println(ct-st);*/
		
		//ann.writeWeightstoFile();
		
		/*final int Size = 204800;
		final float[] huge = new float[Size];
		for (int i=0; i<huge.length; i++) {
			huge[i] = i;
		}
		final float[] huge1 = new float[Size];
		final float[] huge2 = new float[Size];
		final int[] p = new int[1];
		
		Kernel k1 = new Kernel() {
			@Override
			public void run() {
				int gid = getGlobalId();
				//int pass = getPassId();
				int pass = p[0];
				huge1[gid] = huge[gid]*pass;
			}
		};
		
		Kernel k2 = new Kernel() {
			@Override
			public void run() {
				int gid = getGlobalId();
				//int pass = getPassId();
				int pass = p[0];
				huge2[gid] = huge1[gid]*pass;
			}
		};

		Range range = device.createRange(Size);
		k1.setExplicit(true);
		k1.execute(range);
		k2.setExplicit(true);
		k2.execute(range);
		
		//k1.put(huge).put(huge1);
		//k2.put(huge1).put(huge2);
		long st=System.currentTimeMillis();
		for (int i=0; i<1000; i++) {
			p[0]=i;
			k1.execute(range);
			k2.execute(range);
			if (i == 998)
				huge[1]=1;
			
		}
		//k1.get(huge).get(huge1);
		//k2.get(huge1).get(huge2);
		long ct=System.currentTimeMillis();
		System.out.println(ct-st);
		
		System.out.println(huge2[1]);*/
	}
	
}
