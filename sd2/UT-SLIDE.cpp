
/*==========================================================================*/
/*                                                                          */
/* Decoupled and Couplded Block Sliding Analysis Program- UT SLIDE          */
/*                                                                          */
/* Created:    May/04/2004                                                  */
/* Modified :  July/23/2005                                                 */
/* Description : Program for calculationg sliding displacement with         */
/*               deformable sliding mass assumption.                        */
/* Contact : Lee, Yong Woo     +82-10-3213-8722 (mobile phone)              */
/* E-mail : mildbreeze@empal.com                                            */
/*==========================================================================*/



#include <iostream>
#include <fstream>
#include <cmath>
#include <cstring>
#include <iomanip>
#include <cstdlib>
using namespace std;


void decoupled(char filename2[], char filename3[], double &uwgt, double &height, double vs, double damp1, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[],int dv2, int dv3, double vs1);

void d_response(double omega, double damp, double rho, double height,int j, double dt, double acc1, double acc2, double avgacc[], double u[], double udot[], double udotdot[], double u1, double udot1, double udotdot1, double vs);

void d_setupstate(int j, double g, double scal, double &u1, double &udot1, double &udotdot1, double u[], double udot[], double udotdot[], double &acc1, double &acc2, double ain[], double s[],int slide);

void d_sliding(int qq, int j, double dt, int &slide, double mu[], double g, double avgacc[], double sdot[], double s[]);

void d_eq(char filename2[], double n, double o, int npts, double g, double scal, double u1, double udot1, double udotdot1, double u[], double udot[], double udotdot[], double acc1, double acc2, double ain[], double s[], int slide, double omega, double &damp, double rho, double height, double dt, double avgacc[], double &vs, double gameff1, double gamref, double dampf, double pi, double uwgt, double vr, int dv2, double vs1);

void coupled(char filename2[], char filename3[], double &uwgt, double &height, double vs, double damp1, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[], int dv2, int dv3, double vs1);

int slidestop(double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int slide, double pi, double &normalf2,double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma, double dt, double g, double scal, double ain[5000], double angle, double damp);

void solvu(double &u1,double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2,double delt,double dt, double &acc11,double &acc22, int slide,int j,double Mtot, double M, double L, double omega, double beta, double gamma, double damp, double u[]);

void coupled_setupstate(int j, double g, int qq, double scal, double pi, double &u1, double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2, double &s1, double &sdot1, double &sdotdot1,double &s2, double &sdot2, double &sdotdot2, double &normalf1, double &normalf2, double &acc11, double &acc22, double mu[], double ain[], int slide, double Mtot);

void c_slidingcheck(double basef, double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int &slide, double pi, double normalf2, double Mtot, double M, double L, double omega, double mu[], double beta, double gamma, double dt, double g, double scal, double ain[], double angle, double damp);

void c_slideacc(int slide, int j, double dt, int qq, double sdotdot1, double sdot1, double s1, double &sdotdot2, double &sdot2, double &s2, double ain[], double g, double scal, double pi, double mu[], double normalf2, double Mtot, double L, double udotdot2, double &basef);

void c_eq(char filename2[],int npts, double height, double g, int qq, double scal, double pi, double u1, double udot1, double udotdot1, double u2, double udot2, double udotdot2, double s1, double sdot1, double sdotdot1, double s2, double sdot2, double sdotdot2, double normalf1, double normalf2, double acc11, double acc22, double mu[], double ain[], int slide, double Mtot, double delt, double dt, double M, double L, double omega, double beta, double gamma, double &damp, double gameff1, double gamref, double dampf, double &vs, double n, double o, double uwgt, double u[], int dv2, double vr, double vs1);

void residual_mu(int j, int nmu, int slide, double s[], double disp[], int &qq);

void effstr(double u[], double &gameff1, double height, int npts);

void acctime(char filename3[], int npts, int npl, int nhead, char junk[], double ain[]);

int input(char filename1[], char filename2[], char filename3[], double &uwgt, double &height, double &vs, double &damp, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[], int &dv1, int &dv2, int &dv3);

void output1(char filename2[], char descrip[], double rho, double height, double vs, double vr, double damp, int nmu, double mu[], double disp[], double dampf, int dv1, int dv2, int dv3);

void output2(char filename2[], double s[], int j, double dt);

void eq_property(double gameff1, double gamref, double &dampf, double &damp, double &vs, double &n, double &o, double g, double pi, double uwgt, double vr, int dv2, double vs1);

void avg_acc(double avgacc[], int npts, double &mmax);


int main()
{

char filename1[80], filename2[80], filename3[80];
double uwgt,height, vs, damp, dt, scal, g, vr, disp[5], mu[5], vs1;
int dv1, dv2, dv3, npts, nhead, npl,nmu, x;


x=input(filename1, filename2, filename3, uwgt, height, vs, damp, dt, npts, scal, g, nhead, npl, vr, nmu, disp, mu, dv1, dv2, dv3);

vs1=vs;

	if(x==0)
	{
		cout<<"Please check your input file"<<endl;
	}
	else
	{
		if(dv1==0)
		{
			decoupled(filename2, filename3, uwgt, height, vs, damp, dt, npts, scal, g, nhead, npl, vr, nmu, disp, mu, dv2, dv3, vs1);
		}
		else if (dv1==1)
		{
			coupled(filename2, filename3, uwgt, height, vs, damp, dt, npts, scal, g, nhead, npl, vr, nmu, disp, mu, dv2, dv3, vs1);
		}
	}

}


void decoupled(char filename2[], char filename3[], double &uwgt, double &height, double vs, double damp1, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[], int dv2, int dv3, double vs1)
{

double rho, pi, dampf, damp;
int j, slide, dv1;

char junk[100], descrip[100];
double Mtot, M, L, omega, avgacc[10000]={0.0};
double ain[10000],s[10000]={0.0},sdot[10000]={0.0};
double u[10000]={0.0},udot[10000]={0.0}, udotdot[10000]={0.0};
double u1=0.0,udot1=0.0,udotdot1=0.0;

double beta, gamma, acc1=0.0, acc2=0.0;
double mmax=0.0,gameff1=0.0;
double gamref;
double n=100.0, o=100.0;
double time;

int qq;

	rho=uwgt/g;

	dv1=0;
	if(dv2==0)
	{
		dampf=0.0;
	}
	else 
	{
		dampf=55.016*pow((vr/vs),-0.9904)/100;
	}

	
///////////////////////////////////////////


	output1(filename2, descrip, rho, height, vs, vr, damp1, nmu, mu, disp, dampf, dv1, dv2, dv3);

	
	vs=vs1;

// Read Acceleration time history
////////////////////////////////////////////////////////////////////////////
	acctime(filename3, npts, npl, nhead, junk, ain);

    for(j=1;j<=npts;j++)
	{
		ain[j-1]=ain[j-1]*-1;
	}
// for each mode calculate constants for Newmark algorithm
/////////////////////////////////////////////////////////////////////////

	beta=0.25;
	gamma=0.5;
	pi=3.141592;
	Mtot=rho*height;
	slide=0;
    qq=1;
	
	omega=pi*vs/(2*height);
	L=-2*rho*height/pi*cos(pi);
	M=rho*height/2;
    
damp=damp1+dampf;
	n=100.0;
	o=100.0;
	gamref=0.13;

// Loop for time steps in time histories

// For Equivalent Linear
	if(dv3==1)
	{
		d_eq(filename2, n, o, npts, g, scal, u1, udot1, udotdot1, u, udot, udotdot, acc1, acc2, ain, s, slide, omega, damp, rho, height, dt, avgacc, vs, gameff1, gamref, dampf, pi, uwgt, vr, dv2, vs1);
	}
	
		
	omega=pi*vs/(2*height);
	if(dv2==0)
	{
		dampf=0.0;
	}
	else 
	{
		dampf=55.016*pow((vr/vs),-0.9904)/100;
	}
	
// For Linear Elastic
	
	
	
	for(j=1;j<=npts;j++)
	{
		        
		d_setupstate(j, g, scal, u1, udot1, udotdot1, u, udot, udotdot, acc1, acc2, ain, s,slide);

		d_response(omega, damp, rho, height, j, dt, acc1, acc2, avgacc, u, udot, udotdot, u1, udot1, udotdot1, vs);
	}


	slide=0;
	time=0.0;

	fstream fout;
	fout.open(filename2,ios_base::out|ios_base::app);
	fout.setf(ios_base::right, ios_base::floatfield);
	fout.setf(ios::fixed);

	avg_acc(avgacc, npts, mmax);
    
	
	
// Calculate decoupled displacements

	for(j=1;j<=npts;j++)
	{
	    d_sliding(qq, j, dt, slide, mu, g, avgacc, sdot, s);

	//output sliding quantities
/////////////////////////////
	
		output2(filename2, s, j, dt);
		residual_mu(j, nmu, slide, s, disp, qq);

	}


}


void d_response(double omega, double damp, double rho, double height,int j, double dt, double acc1, double acc2, double avgacc[], double u[], double udot[], double udotdot[], double u1, double udot1, double udotdot1, double vs)
{

	double khat, gamma, beta, a, b, L, M, Mtot, pi;
	double deltp, deltu, deltudot, deltudotdot, u2, udot2, udotdot2;

	beta=0.25;
	gamma=0.5;
	pi=3.141592;
	Mtot=rho*height;
	omega=pi*vs/(2*height);
	
	L=-2*rho*height/pi*cos(pi);
	M=rho*height/2;
    khat=(omega*omega)+2*damp*omega*gamma/(beta*dt)+1/(beta*(dt*dt));
	a=1/(beta*dt)+2*damp*omega*gamma/beta;
	b=1/(2*beta)+dt*2*damp*omega*(gamma/(2*beta)-1);


	    if(j==1)
		{
			deltp=-L/M*(acc2-acc1);
			deltu=deltp/khat;
			deltudot=gamma/(beta*dt)*deltu;
			u2=deltu;
			udot2=deltudot;
			udotdot2=-(L/M)*acc2-2*damp*omega*udot2-(omega*omega)*u2;
		}

		else
		{
			deltp=-L/M*(acc2-acc1)+a*udot1+b*udotdot1;
			deltu=deltp/khat;
			deltudot=gamma/(beta*dt)*deltu-gamma/beta*udot1+dt*(1-gamma/(2*beta))*udotdot1;
			deltudotdot=1/(beta*(dt*dt))*deltu-1/(beta*dt)*udot1-0.5/beta*udotdot1;
			u2=u1+deltu;
			udot2=udot1+deltudot;
			udotdot2=udotdot1+deltudotdot;
	
			//
		}

			avgacc[j-1]=acc2;
			u[j-1]=u2;
			udot[j-1]=udot2;
			udotdot[j-1]=udotdot2;
			avgacc[j-1]=avgacc[j-1]+L/Mtot*udotdot[j-1];

}


void d_setupstate(int j, double g, double scal, double &u1, double &udot1, double &udotdot1, double u[], double udot[], double udotdot[], double &acc1, double &acc2, double ain[], double s[],int slide)
{

	//set up state from previous time step
		if(j==1)
		{
		    u1=0.0;
			udot1=0.0;
			udotdot1=0.0;
			
		}
		else
		{
			
			u1=u[j-2];
			udot1=udot[j-2];
			udotdot1=udotdot[j-2];
			
		}


// Set up acceleration loading
		///////////////////////////////////////


		if(j==1)
		{
			acc1=0.0;
			acc2=ain[j-1]*g*scal;
		}
		else if(slide==0)
		{
			acc1=ain[j-2]*g*scal;
			acc2=ain[j-1]*g*scal;
			s[j-1]=s[j-2];
		}
		else
		{
			acc1=ain[j-2]*g*scal;
			acc2=ain[j-1]*g*scal;
		}
	
}


void d_sliding(int qq, int j, double dt, int &slide, double mu[], double g, double avgacc[], double sdot[], double s[])
{

// Calculate decoupled displacements
	double deltacc;

	if(j==1)
	{
		deltacc=avgacc[j-1];
	}
	else
	{
		deltacc=avgacc[j-1]-avgacc[j-2];
	}
   	

	if(slide==0)
	{
		sdot[j-1]=0;
		s[j-1]=s[j-2];
	}

	
	if(slide==1)
	{
		sdot[j-1]=sdot[j-2]+(mu[qq-1]*g-avgacc[j-2])*dt-0.5*deltacc*dt;
		s[j-1]=s[j-2]-sdot[j-2]*dt-0.5*dt*dt*(mu[qq-1]*g-avgacc[j-2])+deltacc*dt*dt/6;
	}

	
	if(slide==0)
	{
		if(avgacc[j-1]>mu[qq-1]*g)
		{
			slide=1;
		}

	}
	else if(slide==1)
		{
			if(sdot[j-1]>=0.0)
			{
				slide=0;
				s[j-1]=s[j-2];
				sdot[j-1]=0.0;
			}
  
		}

}


void d_eq(char filename2[], double n, double o, int npts, double g, double scal, double u1, double udot1, double udotdot1, double u[], double udot[], double udotdot[], double acc1, double acc2, double ain[], double s[], int slide, double omega, double &damp, double rho, double height, double dt, double avgacc[], double &vs, double gameff1, double gamref, double dampf, double pi, double uwgt, double vr, int dv2, double vs1)
{

	int j, t=0;
	
	while(n>5||o>5)
	{
		for(j=1;j<=npts;j++)
			{
				d_setupstate(j, g, scal, u1, udot1, udotdot1, u, udot, udotdot, acc1, acc2, ain, s,slide);
				d_response(omega, damp, rho, height, j, dt, acc1, acc2, avgacc, u, udot, udotdot, u1, udot1, udotdot1, vs);
			}
		
		for(j=1;j<=npts;j++)
			{
				
				effstr(u, gameff1, height, npts);
			}
		
			eq_property(gameff1, gamref, dampf, damp, vs, n, o, g, pi, uwgt, vr, dv2, vs1);

	fstream fout;
	fout.open(filename2,ios_base::out|ios_base::app);
	fout.setf(ios_base::right, ios_base::floatfield);
	fout.setf(ios::fixed);
		
	t=t+1;
	fout<<"ITERATION"<<setw(3)<<t<<setw(10)<<setprecision(2)<<vs<<setw(20)<<setprecision(2)<<vr<<setw(20)<<setprecision(4)<<damp-dampf<<setw(20)<<setprecision(4)<<dampf<<setw(20)<<setprecision(4)<<damp<<endl<<endl;


	}

}


void residual_mu(int j, int nmu, int slide, double s[], double disp[], int &qq)
{

	if(nmu>1)
	{
		if((slide==0)&&(fabs(s[j-1])>=disp[qq-1]))
		{
			if(qq>nmu-1)
			{
				qq=qq;
			}
			else
			{
				qq=qq+1;
			}
		}
	}

}


void effstr(double u[], double &gameff1, double height, int npts)
{
//effective shear strain calculation

	double mx1=0.0, mx=0.0, mmax;
	int j;
		
	for(j=1;j<=npts;j++)
	{
	if (j==1)
	{
		mx1=u[j-1];
		mx=u[j-1];
		
	}
	else
	{
		if(u[j-1]<0)
		{
			if(u[j-1]<=mx1)
               mx1=u[j-1];
			else
				mx1=mx1;
		}
		else
		{
			if(u[j-1]>=mx)
                mx=u[j-1];
			else
				mx=mx;
		}
		
	}


	if(j==npts)
	{
		if(fabs(mx)>fabs(mx1))
		{	
			mmax=mx;
			gameff1=0.65*mmax/height;
		}
		else if(fabs(mx)<fabs(mx1))
		{
			mmax=mx1;
			gameff1=0.65*mmax/height;
		}
		else 
		{
			if(mx>0)
			{
				mmax=mx;
				gameff1=0.65*mmax/height;
			}
			else
			{
				mmax=mx1;
				gameff1=0.65*mmax/height;
			}
		}
	}
	}

gameff1=fabs(gameff1);

}


void acctime(char filename3[], int npts, int npl, int nhead, char junk[], double ain[])
{

	int i, k, j, kk;
	fstream fin;
	fin.open(filename3,ios_base::in);
	
	for(i=1;i<=nhead;i++)
	{
		fin.getline(junk,80);
	}
	k=npts/npl;

	for(i=1;i<=k;i++)
		{	
			for(j=1;j<=npl;j++)
			fin>>ain[(i-1)*npl+j-1];
	 
		}

	kk=npts-k*npl;


	if(!(kk==0))
	{
		for(j=1;j<=kk;j++)
			fin>>ain[k*npl+j-1];
	
	}

}


int input(char filename1[], char filename2[], char filename3[], double &uwgt, double &height, double &vs, double &damp, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[], int &dv1, int &dv2, int &dv3)
{

char descrip[80];
int i;
double a, b, c;

	cout<<"input file? : ";
	cin>>filename1;
	cout<<"output file? : ";
	cin>>filename2;

/////////////////////////////////////////
	fstream fin; 
	fin.open(filename1,ios_base::in);
	fin.getline(descrip,80);
	fin>>dv1>>dv2>>dv3;
	fin>>uwgt>>height>>vs>>damp;
	fin>>dt>>npts>>scal>>g;
	fin>>nhead>>npl;
	fin>>filename3;
	fin>>nmu;

	for(i=1;i<=nmu;i++)
	{
		fin>>disp[i-1]>>mu[i-1];
	}

	fin>>vr;

	a=vr;
	b=vs;
	c=vr/vs;

fin.close();
fin.clear();

	if(c<=2.5)
	{
		cout<<" Shear wave velocity of Rock(VR) should be at least 2.5 times larger than Shear wave velocity of Soil (Vs)"<<endl;
		return 0;	
	}

	if(c>2.5)
	{

		return 1;
	}

}


void output1(char filename2[], char descrip[], double rho, double height, double vs, double vr, double damp, int nmu, double mu[], double disp[], double dampf, int dv1, int dv2, int dv3)
{

	int i;	

	fstream fout;
	fout.open(filename2,ios_base::out);
	fout.setf(ios_base::right, ios_base::floatfield);
	fout<<endl<<endl;
	
	if(dv1==0&&dv2==0&&dv3==0)
	{
		fout<<"Decoupled Linear Elastic Analysis - Rigid Rock Base"<<endl<<endl;
	}
	else if(dv1==0&&dv2==1&&dv3==0)
	{
		fout<<"Decoupled Linear Elastic Analysis  - Elastic Rock Base"<<endl<<endl;
	}
	else if(dv1==0&&dv2==0&&dv3==1)
	{
		fout<<"Decoupled Equivalent Linear Analysis - Rigid Rock Base"<<endl<<endl;
	}

	else if(dv1==0&&dv2==1&&dv3==1)
	{
		fout<<"Decoupled Equivalent Linear Analysis - Elastic Rock Base"<<endl<<endl;
	}
	else if(dv1==1&&dv2==0&&dv3==0)
	{
		fout<<"Coupled Linear Elastic Analysis - Rigid Rock Base"<<endl<<endl;
	}
	else if(dv1==1&&dv2==1&&dv3==0)
	{
		fout<<"Coupled Linear Elastic Analysis - Elastic Rock Base"<<endl<<endl;
	}
	else if(dv1==1&&dv2==0&&dv3==1)
	{
		fout<<"Coupled Equivalent Linear Analysis - Rigid Rock Base "<<endl<<endl;
	}

	else if(dv1==1&&dv2==1&&dv3==1)
	{
		fout<<"Coupled Equivalent Linear Analysis - Elastic Rock Base"<<endl<<endl;
	}

    
	fout<<descrip[80];
	fout<<endl<<endl;
	fout<<"Density : "<<rho<<endl;
	fout<<"Height : "<<height<<endl;

	if(nmu==1)
	{
		fout<<"Yield Acceleration Coeff. : "<<mu[0]<<endl;
	}
	if(!(nmu==1))
	{
		for(i=1;i<=nmu;i++)
		{
			fout<<"Yield Acceleration Coeff.: "<<mu[i-1]<<"   over Displacement "<<disp[i-1]<<endl;
		}
	
	}

	fout<<endl;
	fout<<"Dynamic Properties"<<endl<<endl;
	fout<<setw(38)<<"Shear Wave Velocity"<<setw(42)<<"Damping Ratio"<<endl<<endl;
	fout<<setw(20)<<"Soil"<<setw(20)<<"Rock"<<setw(20)<<"Soil"<<setw(20)<<"Foundation"<<setw(20)<<"Total"<<endl<<endl;
	fout<<"INITIAL"<<setw(13)<<setprecision(4)<<vs<<setw(20)<<setprecision(4)<<vr<<setw(20)<<setprecision(4)<<damp<<setw(20)<<setprecision(4)<<dampf<<setw(20)<<setprecision(4)<<(damp+dampf)<<endl<<endl;

}


void output2(char filename2[], double s[], int j, double dt)
{

	fstream fout;
	fout.open(filename2,ios_base::out|ios_base::app);
	fout.setf(ios_base::right, ios_base::floatfield);
	fout.setf(ios::fixed);
		
	if(j==1)
	{
	fout<<setw(10)<<"TIME"<<setw(30)<<"Sliding Displ."<<endl;
	}
	
	fout<<setw(10)<<setprecision(4)<<j*dt<<setw(25)<<setprecision(5)<<s[j-1]<<endl;
	
}

void eq_property(double gameff1, double gamref, double &dampf, double &damp, double &vs, double &n, double &o, double g, double pi, double uwgt, double vr, int dv2, double vs1)
{

	double gameff2, vs2, com1, com2, damp2, G1, G2, l, m;
	
	gameff2=fabs(gameff1)*100.0;
	vs2=vs1/sqrt(1+(gameff2/gamref));
	com1=1/(1+gameff2/gamref);
	com2=pow(com1,0.1);
	

	if(dv2==0)
		{
			dampf=0.0;
		}
	else if(dv2==1)
		{
			dampf=55.016*pow((vr/vs2),-0.9904);
		}
	
		
	damp2=dampf+0.62*com2*(100/pi*(4*((gameff2-gamref*log((gamref+gameff2)/gamref))/(gameff2*gameff2/(gameff2+gamref)))-2))+1;
    
	G1=(uwgt/g)*vs*vs;
	G2=(uwgt/g)*vs2*vs2;
	
	l=(G1-G2)/G1;
	m=((damp*100)-damp2)/(damp*100);
	
	n=fabs(l)*100.0;
	o=fabs(m)*100.0;
	
	vs=vs2;
	damp=damp2*0.01;
	dampf=dampf*0.01;
    	
}


void coupled(char filename2[], char filename3[], double &uwgt, double &height, double vs, double damp1, double &dt, int &npts, double &scal, double &g, int &nhead, int &npl, double &vr, int &nmu, double disp[], double mu[],int dv2, int dv3, double vs1)
{

	double Mtot, M, L, omega, beta, gamma,angle=0.0;
	int qq,i, dv1;
	double ain[5000];
	char descrip[100], junk[100];
	double rho, pi, delt=0.0, dampf, damp;
	int j=0, slide;

//slide=0 no sliding, slide=1 sliding
//variable that end in 1 are for previous time step
//variable that end in 2 are for current time step


	double s1=0.0, sdot1=0.0, sdotdot1=0.0;
	double s2=0.0, sdot2=0.0, sdotdot2=0.0;
	double u1=0.0, udot1=0.0, udotdot1=0.0;
	double u2=0.0, udot2=0.0, udotdot2=0.0, baseacc=0.0;
	double basef=0.0, acc11=0.0, acc22=0.0, normalf1=0.0, normalf2=0.0;
	double gameff1=0.0, gamref,n, o;
	double s[5000]={0.0},u[5000]={0.0},udotdot[5000]={0.0};
//These are previous iteration value


////////////////////////////////////
//input(filename1, filename2, filename3, uwgt, height, vs, damp, dt, npts, scal, g, nhead, npl, vr, nmu, disp, mu);
	dv1=1;

	rho=uwgt/g;

	if(dv2==0)
	{
		dampf=0.0;
	}
	else
	{
		dampf=55.016*pow((vr/vs),-0.9904)/100;
	}
////////////////////////////////////////////


	output1(filename2, descrip, rho, height, vs, vr, damp1, nmu, mu, disp, dampf, dv1, dv2, dv3);

	


// Read accleration time history
////////////////////////////////////////////////////////////////////////////

	acctime(filename3, npts, npl, nhead, junk, ain);

// for each mode calculate constants for Newmark algorithm
/////////////////////////////////////////////////////////////////////////

	beta=0.25;
	gamma=0.5;
	pi=3.14159;
	Mtot=rho*height;
	slide=0;
	normalf2=0.0;
	angle=0.0;
//qq indicates which mu is in effect
    qq=1;
    omega=pi*vs/(2*height);
	L=2*rho*height/pi;
	M=rho*height/2;
	damp=damp1+dampf;
	n=100.0;
	o=100.0;
	gamref=0.13;

// Finding Equivalent Linear Properties of Soil

	if(dv3==1)
	{
		c_eq(filename2, npts, height, g, qq, scal, pi, u1, udot1, udotdot1, u2, udot2, udotdot2, s1, sdot1, sdotdot1, s2, sdot2, sdotdot2, normalf1, normalf2, acc11, acc22, mu, ain, slide, Mtot, delt, dt, M, L, omega, beta, gamma, damp, gameff1, gamref, dampf, vs, n, o, uwgt, u, dv2, vr, vs1);
	}

// Loop for time steps in time histories

	s1=0.0, sdot1=0.0, sdotdot1=0.0;
	s2=0.0, sdot2=0.0, sdotdot2=0.0;
	u1=0.0, udot1=0.0, udotdot1=0.0;
	u2=0.0, udot2=0.0, udotdot2=0.0, baseacc=0.0;
	basef=0.0, acc11=0.0, acc22=0.0, normalf1=0.0, normalf2=0.0;
	gameff1=0.0;
	omega=pi*vs/(2*height);


  for(i=1;i<=npts;i++)
 {
	 s[npts-1]=0.0;
	 u[npts-1]=0.0;
 }


	for(j=1;j<=npts;j++)
	{
		
		coupled_setupstate(j, g, qq, scal, pi, u1, udot1, udotdot1, u2, udot2, udotdot2, s1, sdot1, sdotdot1,s2, sdot2, sdotdot2, normalf1, normalf2, acc11, acc22, mu, ain, slide, Mtot);

	// Solve for u, udot, udotdot at next time step
	////////////////////////////////////////////////

		solvu(u1, udot1, udotdot1, u2, udot2, udotdot2, delt, dt, acc11,acc22, slide, j, Mtot, M, L, omega, beta, gamma, damp, u);

		udotdot[j-1]=udotdot2;

	///// Update sliding acceleration based on calc'd response
		
		c_slideacc(slide, j, dt, qq, sdotdot1, sdot1, s1, sdotdot2, sdot2, s2, ain, g, scal, pi, mu, normalf2, Mtot, L, udotdot2, basef);

	/// Check if sliding has started

		c_slidingcheck(basef, s1, sdot1, sdotdot1, sdotdot2, u1, udot1, udotdot1, s2, sdot2, u2, udot2, udotdot2, j, qq, slide, pi, normalf2, Mtot, M, L, omega, mu, beta, gamma, dt, g, scal, ain, angle, damp);

		s[j-1]=s2;

		output2(filename2, s, j, dt);

		residual_mu(j, nmu, slide, s, disp, qq);

	}

}

/////////////////////////////////////////////////////////////////	
//      Subroutine for the end of sliding

int slidestop(double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int slide, double pi, double &normalf2,double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma, double dt, double g, double scal, double ain[5000], double angle, double damp)

{
	double ddt,acc11,acc22;
	double acc1b,delt,dd;
	double khat, deltp, a, b;
	double u[8000]={0.0};
	
	delt=dt;
    

	//// Time of end of sliding is taken as where sdot=0 from previous
	//// analysis assuming sliding thruoughout the time step
    ///////////////////////////////////////////////////////////////////
	dd=-sdot1/(sdot2-sdot1);
	ddt=dd*delt;
	acc11=g*sin(angle*pi/180)-mu[qq-1]*(g*cos(angle*pi/180)+ain[j-1]*scal*g*sin(angle*pi/180));
	acc1b=ain[j-2]*g*scal+dd*(ain[j-1]-ain[j-2])*g*scal;
	acc22=g*sin(angle*pi/180)-mu[qq-1]*(g*cos(angle*pi/180)+acc1b*sin(angle*pi/180));


	//if dd=0, sliding has already stopped and skip this solution

	if(dd==0)
	{
		return 0;
	}

	

    solvu(u1, udot1, udotdot1, u2, udot2, udotdot2, delt, ddt, acc11,acc22, slide, j, Mtot, M, L, omega, beta, gamma, damp, u);
    u1=u2;
	udot1=udot2;
	udotdot1=udotdot2;
	normalf2=Mtot*g*cos(angle*pi/180)+Mtot*acc1b*sin(angle*pi/180);
	sdotdot2=-acc1b*cos(angle*pi/180)-mu[qq-1]*normalf2/Mtot-L*udotdot2/Mtot+g*sin(angle*pi/180);
	sdot2=sdot1+0.5*ddt*(sdotdot2+sdotdot1);
	s2=s1+0.5*ddt*(sdot1+sdot2);

	// Solve for non sliding response during remaining part of dt
	////////////////////////////////////////////////////////////////

	ddt=(1-dd)*delt;
	slide=0;
	acc11=acc22;
	acc22=ain[j-1]*g*scal*cos(angle*pi/180);

	khat=1+2*damp*omega*gamma*ddt+(omega*omega)*beta*(ddt*ddt);
	a=(1-(L*L)/(Mtot*M))+2*damp*omega*ddt*(gamma-1)+(omega*omega)*(ddt*ddt)*(beta-0.5);
	b=(omega*omega)*ddt;
	deltp=-L/M*(acc22-acc11)+a*(udotdot1)-b*(udot1);
	udotdot2=deltp/khat;

	udot2=udot1+(1-gamma)*ddt*(udotdot1)+gamma*ddt*(udotdot2);
	u2=u1+udot1*ddt+(0.5-beta)*(ddt*ddt)*(udotdot1)+beta*(ddt*ddt)*(udotdot2);
}


////////////////////////////////////////////////////////////////
//solves for u, udot, and udotdot at next time step
void solvu(double &u1,double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2,double delt,double dt, double &acc11,double &acc22, int slide,int j,double Mtot, double M, double L, double omega, double beta, double gamma, double damp, double u[])
{
	double khat,a,b,deltp,deltu,deltudot;
	double d1;

	delt = dt;

	if(slide==1)
	{
		d1=1-(L*L)/(M*Mtot);
	} 
	else
	{
		d1=1.0;
	}

	khat=(omega*omega)+2*damp*omega*gamma/(beta*delt)+d1/(beta*(delt*delt));
	a=d1/(beta*delt)+2*damp*omega*gamma/beta;
	b=d1/(2*beta)+delt*2*damp*omega*(gamma/(2*beta)-1);

	if(j==1)
	{
		deltp=-L/M*(acc22-acc11);
		deltu=deltp/khat;
		deltudot=gamma/(beta*delt)*deltu;
		u2=deltu;
		udot2=deltudot;
		udotdot2=(-(L/M)*acc22-2*damp*omega*udot2-(omega*omega)*u2)/d1;
	}
	else
	{
		deltp=-L/M*(acc22-acc11)+a*udot1+b*udotdot1;
		deltu=deltp/khat;
		deltudot=gamma/(beta*delt)*deltu-gamma/beta*udot1+delt*(1-gamma/(2*beta))*udotdot1;
		u2=u1+deltu;
		udot2=udot1+deltudot;
		udotdot2=(-(L/M)*acc22-2*damp*omega*udot2-(omega*omega)*u2)/d1;
	}

		u[j-1]=u2;


}


void coupled_setupstate(int j, double g, int qq, double scal, double pi, double &u1, double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2, double &s1, double &sdot1, double &sdotdot1,double &s2, double &sdot2, double &sdotdot2, double &normalf1, double &normalf2, double &acc11, double &acc22, double mu[], double ain[], int slide, double Mtot)
{
	
	double angle=0.0;
	
	
		
		// set up state from previous time step
		if(j==1)
		{
			u1=0.0;
			udot1=0.0;
			udotdot1=0.0;
			s1=0.0;
			sdot1=0.0;
			sdotdot1=0.0;
			normalf1=0.0;
		}
		
		else
		{
			u1=u2;
			udot1=udot2;
			udotdot1=udotdot2;
			s1=s2;
			sdot1=sdot2;
			sdotdot1=sdotdot2;
			normalf1=normalf2;
		}


// Set up acceleration loading
///////////////////////////////////////

// Normal force corrected for vertical component of accel
/////////////////////////////////////////////////////////

	normalf2=Mtot*g*cos(angle*pi/180)+Mtot*ain[j-1]*scal*g*sin(angle*pi/180);

	if(j==1)
		{
			acc11=0.0;
			acc22=ain[j-1]*g*scal*cos(angle*pi/180);
		}
	else if (slide==0)
		{
			acc11=ain[j-2]*g*scal*cos(angle*pi/180);
			acc22=ain[j-1]*g*scal*cos(angle*pi/180);
		}	
	else
		{
			acc11=g*sin(angle*pi/180)-mu[qq-1]*normalf1/Mtot;
			acc22=g*sin(angle*pi/180)-mu[qq-1]*normalf2/Mtot;
		}
	
}


void c_slidingcheck(double basef, double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int &slide, double pi, double normalf2, double Mtot, double M, double L, double omega, double mu[], double beta, double gamma, double dt, double g, double scal, double ain[], double angle, double damp)
{
/// Check if sliding has started

		if(slide==0)
		{
			if(basef>mu[qq-1]*normalf2)
			{
				slide=1;
			}
		}
		else if(slide==1)
		{
			if(sdot2<=0.0)
			{
				
				slidestop(s1, sdot1, sdotdot1, sdotdot2, u1, udot1, udotdot1, s2, sdot2, u2, udot2, udotdot2, j, qq, slide, pi, normalf2, Mtot, M, L, omega, mu, beta, gamma, dt, g, scal, ain, angle, damp);
				
				slide=0;
				sdot2=0.0;
				sdotdot2=0.0;
			}
		}

}


void c_slideacc(int slide, int j, double dt, int qq, double sdotdot1, double sdot1, double s1, double &sdotdot2, double &sdot2, double &s2, double ain[], double g, double scal, double pi, double mu[], double normalf2, double Mtot, double L, double udotdot2, double &basef)
{
	///// Update sliding acceleration based on calc'd response

		double angle=0.0;

		if(slide==1)	
		{
			sdotdot2=-ain[j-1]*g*scal*cos(angle*pi/180)-mu[qq-1]*normalf2/Mtot-L*udotdot2/Mtot+g*sin(angle*pi/180);

		}


////// Calc. base force based on udotdot calc

		basef=-Mtot*ain[j-1]*scal*g*cos(angle*pi/180)-L*udotdot2+Mtot*g*sin(angle*pi/180);

///// If sliding is occuring, integrate sdotdot,
///// using trapezoid rule, to get sdot and s
//////////////////////////////////////////////////

		if(slide==1)
		{
			sdot2=sdot1+0.5*dt*(sdotdot2+sdotdot1);
			s2=s1+0.5*dt*(sdot2+sdot1);
		}

}


void c_eq(char filename2[],int npts, double height, double g, int qq, double scal, double pi, double u1, double udot1, double udotdot1, double u2, double udot2, double udotdot2, double s1, double sdot1, double sdotdot1, double s2, double sdot2, double sdotdot2, double normalf1, double normalf2, double acc11, double acc22, double mu[], double ain[], int slide, double Mtot, double delt, double dt, double M, double L, double omega, double beta, double gamma, double &damp, double gameff1, double gamref, double dampf, double &vs, double n, double o, double uwgt, double u[], int dv2, double vr, double vs1)
{

	int t=0, j;

	while(n>5||o>5)
	{
		for(j=1;j<=npts;j++)
		{

			coupled_setupstate(j, g, qq, scal, pi, u1, udot1, udotdot1, u2, udot2, udotdot2, s1, sdot1, sdotdot1,s2, sdot2, sdotdot2, normalf1, normalf2, acc11, acc22, mu, ain, slide, Mtot);
			solvu(u1, udot1, udotdot1, u2, udot2, udotdot2, delt, dt, acc11,acc22, slide, j, Mtot, M, L, omega, beta, gamma, damp, u);
				
		}
	

		for(j=1;j<=npts;j++)
		{
			
			effstr(u, gameff1, height, npts);
		}

			eq_property(gameff1, gamref, dampf, damp, vs, n, o, g, pi, uwgt, vr, dv2, vs1);


	fstream fout;
	fout.open(filename2,ios_base::out|ios_base::app);
	fout.setf(ios_base::right, ios_base::floatfield);
	fout.setf(ios::fixed);
	
	t=t+1;
	fout<<"ITERATION"<<setw(3)<<t<<setw(10)<<setprecision(2)<<vs<<setw(20)<<setprecision(2)<<vr<<setw(20)<<setprecision(4)<<damp-dampf<<setw(20)<<setprecision(4)<<dampf<<setw(20)<<setprecision(4)<<damp<<endl<<endl;

	}

}	


void avg_acc(double avgacc[], int npts, double &mmax)
{
//effective shear strain calculation

	double mx1=0.0, mx=0.0;
	int j;
	
	for(j=1;j<=npts;j++)
	{
	
		if (j==1)
		{
			mx1=avgacc[j-1];
			mx=avgacc[j-1];
		}
		else
		{
			if(avgacc[j-1]<0)
			{
				if(avgacc[j-1]<=mx1)
					mx1=avgacc[j-1];
				else
					mx1=mx1;
			}	
			else
			{
				if(avgacc[j-1]>=mx)
					mx=avgacc[j-1];
				else
					mx=mx;
			}
		
		}

	
		if(j==npts)
		{
			if(fabs(mx)>fabs(mx1))
			{	
				mmax=mx;
			}
			else if(fabs(mx)<fabs(mx1))
			{
				mmax=mx1;
			}
			else 
			{
				if(mx>0)
				{
					mmax=mx;
				}
				else
				{
					mmax=mx1;
				}
			}
		}
	}

}

