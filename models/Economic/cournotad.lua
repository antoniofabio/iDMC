--%  ft| BIFURCATION_1 sn| Bifurcation~diagram n| #0 d| 0.01 n| #1 d| 0.01 n| #2 d| 1 n| #3 d| 0.6 n| #4 d| b n| #5 d| 10 n| #6 d| 26 n| #7 d| 0.01 n| #8 d| 0.1 n| #9 d| 500 n| #10 d| 200 n| #11 d| x 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Quasiperiodic~trajectory n| #0 d| 0.01 n| #1 d| 0.01 n| #2 d| 1 n| #3 d| 12 n| #4 d| 0.6 n| #5 d| 100 n| #6 d| 5000 n| #7 d| 5000 n| #8 d| x n| #9 d| y 
--@@
name = "Adaptive Cournot Oligopoly"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a", "b","c"}
variables ={"x", "y"}

function f(a, b, c, x, y)

	if (a*y <= 1) then 

 	x1= (1-c)*x+c*(math.sqrt(y/a)-y)

	else
	x1=(1-c)*x
	end

	if (b*x <= 1) then 

	y1= (1-c)*y+c*(math.sqrt(x/b)-x)
	
	else
	y1=(1-c)*y
	end

return x1, y1

end

function Jf(a,b,c,x,y)

dxdx = 1-c
dydy = dxdx

	if (a*y <= 1) then

	dxdy = c*(1/2*math.sqrt(1/a/y)-1)

	else 
	
	dxdy = 0

	end

	if (b*x <= 1) then

	
	dydx = c*(1/2*math.sqrt(1/b/x)-1)

	else

	dydx = 0

	end


return 

	dxdx,	dxdy,
	dydx,	dydy

end










