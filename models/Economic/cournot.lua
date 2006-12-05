--%  ft| BIFURCATION_1 sn| Period~-~doubling~route~to~chaos n| #0 d| 0.01 n| #1 d| 0.01 n| #2 d| 1 n| #3 d| b n| #4 d| 5.77 n| #5 d| 6.25 n| #6 d| -0.01 n| #7 d| 0.17 n| #8 d| 3000 n| #9 d| 200 n| #10 d| x 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 0.01 n| #1 d| 0.01 n| #2 d| 1 n| #3 d| 6.2 n| #4 d| 100 n| #5 d| 5000 n| #6 d| 5000 n| #7 d| x n| #8 d| y 
--@@
name = "Cournot Olygopoly"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a", "b"}
variables ={"x", "y"}

function f(a, b,  x, y)

	if (a*y <= 1) then 

 	x1= math.sqrt(y/a)-y

	else
 	
	x1=0

	end

	if (b*x <= 1) then 

	y1= math.sqrt(x/b)-x
	
	else

	y1=0

	end


return x1, y1

end



function Jf(a,b,x,y)


	if (a*y <= 1) then

	dxdy =math.sqrt(1/(4*a*y))-1

	else 
	
	dxdy = 0

	end


	if (b*x <= 1) then
	
	dydx = math.sqrt(1/(4*b*x))-1	

	else

	dydx = 0

	end


return 	0,	dxdy,
		dydx,	0

end










