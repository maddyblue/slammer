//This program calculates coupled seismic displacements for
//deformable system on an inclined plane.
//Peak to residual Mu capabilities.
//Only one mode modeled

#include <iostream>
#include <fstream>
#include <cmath>
#include <cstring>
#include <iomanip>
#include <cstdlib>
//#include <nodefaultlib>
using namespace std;

void slidestop(double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int slide,  double pi, double &normalf2, double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma1, double dt, double g, double scal, double ain[40000], double angle, double damp);
void solvu(double &u1,double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2,double delt,double &acc11,double &acc22, int slide,int j,double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma1, double dt, double g, double scal, double ain[40000], double angle, double damp, int qq);

double Mtot, M, L, omega, beta, gamma1, dt, g, scal, angle, damp;
int qq;
double mu[5]={0,0,0,0,0}, ain[40000];



int main()
{

double height, rho, vs, uwgt, pi, delt=0, disp[5], time;
int nhead, npl, npts, i, k, j, kk, slide, nmu ;

//slide=0 no sliding, slide=1 sliding
//variable that end in 1 are for previous time step
//variable that end in 2 are for current time step

char filename1[100], filename2[100];
char filename3[100];
char descrip[100],junk[100];

double s1=0, sdot1=0, sdotdot1=0;
double s2=0, sdot2=0, sdotdot2=0;
double u1=0, udot1=0, udotdot1=0;
double u2=0, udot2=0, udotdot2=0, baseacc=0;
double basef=0, acc1=0, acc2=0, normalf1=0, normalf2=0;

//These are previous iteration value


////////////////////////////////////
cout<<"input file? : ";
cin>>filename1;
cout<<"output file? : ";
cin>>filename2;

/////////////////////////////////////////
fstream fin;
fstream fout;
fin.open(filename1,ios_base::in);
fout.open(filename2,ios_base::out);
fout.setf(ios_base::right, ios_base::floatfield);
fin.get(descrip,100);

fin>>uwgt>>height>>vs>>damp;
fin>>dt>>npts>>scal>>g;
fin>>nhead>>npl;
fin>>filename3;
fin>>angle>>nmu;

rho=uwgt/g;

for(i=1;i<=nmu;i++)
{
	fin>>disp[i-1]>>mu[i-1];
}



////////////////////////////////////////////
fout<<descrip[100];
fout<<endl<<endl;
fout<<"Density : "<<rho<<endl;
fout<<"Height : "<<height<<endl;
fout<<"Shear wave velocity : "<<vs<<endl;
fout<<"Damping Ratio : "<<damp<<endl;
if(nmu==1)
{
	fout<<"Friction Coeff. : "<<mu[0]<<endl<<endl;
}
if(!(nmu==1))
{
	for(i=1;i<=nmu;i++)
	{
		fout<<"Friction Coeff. : "<<mu[i-1]<<endl<<endl;
	}

}

fout<<"  "<<"Time"<<"   "<<"Sliding Displ."<<"   "<<"Sliding Veloc."<<"   "<<"Slidng Accel."<<"  "<<"Slide"<<"  "<<"udotdot"<<"  "<<"Ground Acc"<<endl<<endl;
fin.close();
fin.clear();

// Read accleration time history
////////////////////////////////////////////////////////////////////////////
fin.open(filename3,ios_base::in);

for(i=1;i<=nhead;i++)
{
	fin.get(junk,100);
}

k=npts/npl;



for(i=1;i<=k;i++)

	{
		for(j=1;j<=npl;j++)

		{fin>>ain[(i-1)*npl+j-1];


		}
	}

kk=npts-k*npl;
	if(!(kk==0))
	{
		for(j=1;j<=kk;j++)
			fin>>ain[k*npl+j-1];
	}


// for each mode calculate constants for Newmark algorithm
/////////////////////////////////////////////////////////////////////////

	beta=0.25;
	gamma1=0.5;
	pi=3.14159;
	Mtot=rho*height;
	slide=0;
	normalf2=0.0;
//qq indicates which mu is in effect
    qq=1;

		omega=pi*vs/(2*height);
		L=2*rho*height/pi;
		M=rho*height/2;



// Loop for time steps in time histories

	for(j=1;j<=npts;j++)
	{
		time=j*dt;

// set up state from previous time step
		if(j==1)
		{
			u1=0;
			udot1=0;
			udotdot1=0;
			s1=0;
			sdot1=0;
			sdotdot1=0;
			normalf1=0;
		}

		if(!(j==1))
		{
			u1=u2;
			udot1=udot2;
			udotdot1=udotdot2;
			s1=s2;
			sdot1=sdot2;
			sdotdot1=sdotdot2;
			normalf1=normalf2;
		}

cout<<"ain= "<<ain[j-1]<<endl;
// Set up acceleration loading
///////////////////////////////////////

// Normal force corrected for vertical component of accel
/////////////////////////////////////////////////////////

	normalf2=Mtot*g*cos(angle*pi/180)+Mtot*ain[j-1]*scal*g*sin(angle*pi/180);

	if(j==1)
	{
		acc1=0;
		acc2=ain[j-1]*g*scal*cos(angle*pi/180);
	}
	if(!(j==1))
	{
		if(slide==0)
		{
			acc1=ain[j-2]*g*scal*cos(angle*pi/180);
			acc2=ain[j-1]*g*scal*cos(angle*pi/180);
		}
		if(!(slide==0))
		{
			acc1=g*sin(angle*pi/180)-mu[qq-1]*normalf1/Mtot;
			acc2=g*sin(angle*pi/180)-mu[qq-1]*normalf2/Mtot;
		}
	}




// Solve for u, udot, udotdot at next time step
////////////////////////////////////////////////

solvu(u1, udot1, udotdot1, u2, udot2, udotdot2,delt,acc1,acc2, slide, j, Mtot, M, L, omega, mu, beta, gamma1, dt, g, scal, ain , angle, damp, qq);

////// Calc. base force based on udotdot calc

		basef=-Mtot*ain[j-1]*scal*g*cos(angle*pi/180)-L*udotdot2+Mtot*g*sin(angle*pi/180);

/////// Check if sliding has started
		cout<<"basef="<<basef<<" muqq "<<mu[qq-1]*normalf2<<endl;

		if(slide==0)
		{
			if(basef>mu[qq-1]*normalf2)
			{
				slide=1;
			}
		}

///// Update sliding acceleration based on calc'd response

		if(slide==1)
		{
			sdotdot2=-ain[j-1]*g*scal*cos(angle*pi/180)-mu[qq-1]*normalf2/Mtot-L*udotdot2/Mtot+g*sin(angle*pi/180);
		}

///// If sliding is occuring, integrate sdotdot,
///// using trapezoid rule, to get sdot and s
//////////////////////////////////////////////////

		if(slide==1)
		{
			sdot2=sdot1+0.5*dt*(sdotdot2+sdotdot1);
			s2=s1+0.5*dt*(sdot2+sdot1);
		}

///// Check if sliding has stopped. B/c 1 way sliding and know that
///// sdot is >0 for our direction, just check sdot<0

		if(slide==1)
		{
			if(sdot2<=0.0)
			{

				slidestop(s1, sdot1, sdotdot1, sdotdot2, u1, udot1, udotdot1, s2, sdot2, u2, udot2, udotdot2, j, qq, slide, pi, normalf2, Mtot, M, L, omega, mu, beta, gamma1, dt, g, scal, ain, angle, damp);

				slide=0;
				sdot2=0.0;
				sdotdot2=0.0;
			}
		}

		baseacc=ain[j-1]*g*scal*cos(angle*pi/180);


///// Output sliding quantities
//////////////////////////////////
fout.setf(ios::fixed);
//fout<<"  "<<setw(9)<<setprecision(5)<<time<<"   "<<setw(8)<<setprecision(5)<<s2<<"   "<<setw(8)<<setprecision(5)<<sdot2<<"   "<<setw(8)<<setprecision(5)<<sdotdot2<<"  "<<setw(3)<<slide<<"  "<<setw(10)<<setprecision(5)<<udotdot2<<"  "<<setw(10)<<setprecision(5)<<baseacc<<endl<<endl;
fout<<"  "<<setw(9)<<setprecision(5)<<time<<"  "<<setw(10)<<setprecision(5)<<s2<<endl;

if(nmu>1)
{
	if((slide==0)&&(abs(s2)>=disp[qq-1]))
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

return 0;
}

/////////////////////////////////////////////////////////////////
//      Subroutine for the end of sliding

void slidestop(double &s1, double &sdot1, double &sdotdot1, double &sdotdot2, double &u1, double &udot1, double &udotdot1, double &s2, double &sdot2, double &u2, double &udot2, double &udotdot2, int j, int qq, int slide, double pi, double &normalf2,double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma1, double dt, double g, double scal, double ain[40000], double angle, double damp)
//void slidestop(double s1, double sdot1, double sdotdot1, double sdotdot2, double u1, double udot1, double udotdot1, double s2, double sdot2, double u2, double udot2, double udotdot2, int j, int qq, int slide, double L, double M,double damp, double omega, double gamma1, double beta, double Mtot, double mu, double dt, double ain[], double g, double scal, double angle, double pi, double normalf2)
{
	double ddt,acc11,acc22;
	double acc1b,delt,dd;
	double khat, deltp, a, b;

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
		return ;
	}

	slide=1;

   solvu(u1, udot1, udotdot1, u2, udot2, udotdot2,delt,acc11,acc22, slide, j, Mtot, M, L, omega, mu, beta, gamma1, dt, g, scal, ain,angle,damp,qq);

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

	khat=1+2*damp*omega*gamma1*ddt+(omega*omega)*beta*(ddt*ddt);
	a=(1-(L*L)/(Mtot*M))+2*damp*omega*ddt*(gamma1-1)+(omega*omega)*(ddt*ddt)*(beta-0.5);
	b=(omega*omega)*ddt;
	deltp=-L/M*(acc22-acc11)+a*(udotdot1)-b*(udot1);
	udotdot2=deltp/khat;

	udot2=udot1+(1-gamma1)*ddt*(udotdot1)+gamma1*ddt*(udotdot2);
	u2=u1+udot1*ddt+(0.5-beta)*(ddt*ddt)*(udotdot1)+beta*(ddt*ddt)*(udotdot2);
}


////////////////////////////////////////////////////////////////
//solves for u, udot, and udotdot at next time step
void solvu(double &u1,double &udot1, double &udotdot1, double &u2, double &udot2, double &udotdot2,double delt,double &acc11,double &acc22, int slide,int j,double Mtot, double M, double L, double omega, double mu[5], double beta, double gamma1, double dt, double g, double scal, double ain[40000], double angle, double damp, int qq)
{
	double khat,a,b,deltp,deltu,deltudot;
	double d1;

	delt = dt;

	if(slide==1)
	{
		d1=1-(L*L)/(M*Mtot);
	}
	if(!(slide==1))
	{
		d1=1.0;
	}

	khat=(omega*omega)+2*damp*omega*gamma1/(beta*delt)+d1/(beta*(delt*delt));
	a=d1/(beta*delt)+2*damp*omega*gamma1/beta;
	b=d1/(2*beta)+delt*2*damp*omega*(gamma1/(2*beta)-1);

	if(j==1)
	{
		deltp=-L/M*(acc22-acc11);
		deltu=deltp/khat;
		deltudot=gamma1/(beta*delt)*deltu;
		u2=deltu;
		udot2=deltudot;
		udotdot2=(-(L/M)*acc22-2*damp*omega*udot2-(omega*omega)*u2)/d1;
	}
	if(!(j==1))
	{
		deltp=-L/M*(acc22-acc11)+a*udot1+b*udotdot1;
		deltu=deltp/khat;
		deltudot=gamma1/(beta*delt)*deltu-gamma1/beta*udot1+delt*(1-gamma1/(2*beta))*udotdot1;
		u2=u1+deltu;
		udot2=udot1+deltudot;
		udotdot2=( -(L/M)*acc22 - 2*damp*omega*udot2 - (omega*omega)*u2)  /d1;
	}
}



















