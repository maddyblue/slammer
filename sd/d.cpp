#include <iostream>
#include <fstream>
#include <cmath>
#include <cstring>
#include <iomanip>
#include <cstdlib>
using namespace std;

int main()
{

double height, rho, vs, g, dt, damp, scal, mu[5], uwgt, pi, disp[5];
int nhead, npl, i, k, j, kk, npts, slide, qq, nmu;

//slide=0 no sliding, slide=1 sliding

char filename1[100], filename2[100]; 
char filename3[100];
char junk[100], descrip[100];
double Mtot, M, L, omega, avgacc[10000], deltacc;
double ain[10000],s[10000],sdot[10000];
double u[10000]={0},udot[10000]={0}, udotdot[10000]={0};
double u1=0,udot1=0,udotdot1=0;
double u2=0,udot2=0,udotdot2=0;
double beta, gamma, acc1, acc2;
//These are previous iteration value

double khat,a,b,deltp,deltu,deltudot;
double deltudotdot;
double time;

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
fin>>nmu;

for(i=1;i<=nmu;i++)
{
	fin>>disp[i-1]>>mu[i-1];
}

rho=uwgt/g;

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
else
{
	for(i=1;i<=nmu;i++)
	{
		fout<<"Displacement : "<<disp[i-1]<<"   "<<"Friction Coeff. : "<<mu[i-1]<<endl<<endl;
	}
}


fout<<"  Time       Displacement       sliding Velocity       sliding"<<endl;

fin.close();
fin.clear();

// Read Acceleration time history
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
	{
		fin>>ain[(i-1)*npl+j-1];

	} 
	}

	kk=npts-k*npl;


	if(!(kk==0))
	{
		for(j=1;j<=kk;j++)
			fin>>ain[k*npl+j-1];
	        fout<<ain[k*npl+j-1];
	}



// for each mode calculate constants for Newmark algorithm
/////////////////////////////////////////////////////////////////////////

	beta=0.25;
	gamma=0.5;
	pi=3.141592;
	Mtot=rho*height;
	slide=0;
    //qq indicates which mu is in effect
	qq=1;

	omega=pi*vs/(2*height);
	L=-2*rho*height/(pi*cos(pi));
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

		}

		if(!(j==1))
		{

			u1=u[j-2];
			udot1=udot[j-2];
			udotdot1=udotdot[j-2];

		}

// Set up acceleration loading
		///////////////////////////////////////


	if(j==1)
	{
		acc1=0;
		acc2=ain[j-1]*g*scal;
	}
	else
	{
		if(slide==0)
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


// Solve for u, udot, udotdot at next time step
	/////////////////////////////////////////////

//		
		khat=(omega*omega)+2*damp*omega*gamma/(beta*dt)+1/(beta*(dt*dt));
		a=1/(beta*dt)+2*damp*omega*gamma/beta;
		b=1/(2*beta)+dt*2*damp*omega*(gamma/(2*beta)-1);


	    if(j==1)
		{
			deltp=-L/M*(acc2-acc1);
			deltu=deltp/khat;
			deltudot=gamma/(beta+dt)*deltu;
			u2=deltu;
			udot2=deltudot;
			udotdot2=-(L/M)*acc2-2*damp*omega*udot2-(omega*omega)*u2;
		}

		if(!(j==1))
		{
			deltp=-L/M*(acc2-acc1)+a*udot1+b*udotdot1;
			deltu=deltp/khat;
			deltudot=gamma/(beta*dt)*deltu-gamma/beta*udot1+dt*(1-gamma/(2*beta))*udotdot1;
			deltudotdot=1/(beta*(dt*dt))*dt-1/(beta*dt)*udot1-0.5/beta*udotdot1;
			u2=u1+deltu;
			udot2=udot1+deltudot;
			udotdot2=udotdot1+deltudotdot;
			udotdot2=-(L/M)*acc2-2*damp*omega*udot2-(omega*omega)*u2;
		}


			avgacc[j-1]=acc2;

			u[j-1]=u2;
			udot[j-1]=udot2;
			udotdot[j-1]=udotdot2;
			avgacc[j-1]=avgacc[j-1]+L/Mtot*udotdot[j-1];


}



// Calculate decoupled displacements

for(j=1;j<=npts;j++)
{
	time=j*dt;

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

	if(slide==0)
	{
		if(avgacc[j-1]>mu[qq-1]*g)
		{
			slide=1;
		}

	}


	if(slide==1)
	{
		sdot[j-1]=sdot[j-2]+(mu[qq-1]*g-avgacc[j-2])*dt-0.5*deltacc*dt;
		s[j-1]=s[j-2]+sdot[j-2]*dt+0.5*dt*dt*(mu[qq-1]*g-avgacc[j-2])-deltacc*dt*dt/6;
	}



	if(slide==1)
		{
			if(sdot[j-1]>=0.0)
			{
				slide=0;
				s[j-1]=s[j-2];
				sdot[j-1]=0;
			}

		}

//    
//output sliding quantities
/////////////////////////////
	fout.setf(ios::fixed);  
//fout<<time<<"  "<<setw(10)<<setprecision(5)<<s[j-1]<<"  "<<setw(10)<<setprecision(5)<<sdot[j-1]<<"  "<<setw(10)<<setprecision(5)<<slide<<endl;
fout<<time<<"  "<<setw(10)<<setprecision(5)<<s[j-1]<<endl;

if(nmu>1)
{
	if((slide==0)&&(abs(s[j-1])>=disp[qq-1]))
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










