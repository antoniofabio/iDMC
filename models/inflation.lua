--%  ft| BIFURCATION_2 sn| zerocosts n| #0 d| 0 n| #1 d| 0.01 n| #2 d| 0 n| #3 d| 0.01 n| #4 d| 0.5 n| #5 d| 0 n| #6 d| 1 n| #7 d| 0.35 n| #8 d| 0.05 n| #9 d| 0.02 n| #10 d| gamma n| #11 d| 0.68 n| #12 d| 1.3 n| #13 d| lambda n| #14 d| 0 n| #15 d| 40000 n| #16 d| 0.00001 n| #17 d| 10e10 n| #18 d| 26 n| #19 d| 5000 
--%  ft| BASINSLICE_B sn| 1~fixed~point,~1~strange~attractor n| #0 d| 0.95 n| #1 d| 0.5 n| #2 d| 1e-4 n| #3 d| 12500 n| #4 d| 1 n| #5 d| 0.35 n| #6 d| 0.05 n| #7 d| 0.02 n| #8 d| 5000 n| #9 d| 1000 n| #10 d| 20 n| #11 d| 2e-5 n| #12 d| x1 n| #13 d| x2 n| #14 d| -0.05 n| #15 d| 0.11 n| #16 d| -0.05 n| #17 d| 0.11 n| #18 d| 0.01 n| #19 d| 0.01 n| #20 d| 0.01 n| #21 d| 0.01 
--@@
name = "inflation"
description = "inflation model"
type = "D"
parameters = {"gamma", "delta", "C", "lambda", "alpha", "beta", "m", "gn"}
variables = {"x1", "x2", "x3", "x4"}

function f(gamma, delta, C, lambda, alpha, beta, m, gn, x1, x2, x3, x4)
	eq = m - gn
	i = 1/ (1 + alpha * beta) 

	ex0 = x1 + gamma * (x1 - x2)
	rg0 = x1 + delta * (eq - x1)
	ex1 = x2 + gamma * (x2 - x3)
	rg1 = x2 + delta * (eq - x2)
	ex2 = x3 + gamma * (x3 - x4)
	rg2 = x3 + delta * (eq - x3)

	exerr0 = ex1 - x1
	rgerr0 = rg1 - x1
	aex0 = - (exerr0)^2
	arg0 = - (rgerr0)^2 - C
	da0 = arg0 - aex0
	exerr1 = ex2 - x2
	rgerr1 = rg2 - x2
	aex1 = - (exerr1)^2
	arg1 = - (rgerr1)^2 - C
	da1 = arg1 - aex1

	wex0 = 1 / ( 1 + math.exp(lambda  * da0) )
	wrg0 = 1 - wex0
	wex1 = 1 / ( 1 + math.exp(lambda * da1) )
	wrg1 = 1 - wex1
	
	e0 = wex0 * ex0 + wrg0 * rg0
	e1 = wex1 * ex1 + wrg1 * rg1
	de = e0 - e1

	x0 = (alpha * beta * (m - gn) + x1 + de) * i
	return x0, x1, x2, x3
end

function Jf(gamma, delta, C, lambda, alpha, beta, m, gn, x1, x2, x3, x4)

	eq = m - gn
	i = 1/ (1 + alpha * beta)
 	ex0 = x1 + gamma * (x1 - x2)
	rg0 = x1 + delta * (eq - x1)
	ex1 = x2 + gamma * (x2 - x3)
	rg1 = x2 + delta * (eq - x2)
	ex2 = x3 + gamma * (x3 - x4)
	rg2 = x3 + delta * (eq - x3)
	exerr0 = ex1 - x1
	rgerr0 = rg1 - x1
	aex0 = - (exerr0)^2
	arg0 = - (rgerr0)^2 - C
	da0 = arg0 - aex0
	exerr1 = ex2 - x2
	rgerr1 = rg2 - x2
	aex1 = - (exerr1)^2
	arg1 = - (rgerr1)^2 - C
	da1 = arg1 - aex1
	wex0 = 1 / ( 1 + math.exp(lambda  * da0) )
    	wex1 = 1 / ( 1 + math.exp(lambda * da1) )
    	
	y0 = - delta * eq + (delta + gamma) * x1 - gamma * x2
	y1 = - delta * eq + (delta + gamma) * x2 - gamma * x3

	z0 = - lambda * (wex0^2) * math.exp(lambda  * da0)
	z1 = - lambda * (wex1^2) * math.exp(lambda  * da1)

	w01 = z0 * (2*rgerr0 - 2*exerr0)
	w02 = z0 * ((2*delta-2)*rgerr0 + (2*gamma+2)*exerr0) 
	w12 = z1 * (2*rgerr1 - 2*exerr1)
	w03 = z0 * (-2*gamma*exerr0)
	w13 = z1 * ((2*delta-2)*rgerr1 + (2*gamma+2)*exerr1)
	w14 = z1 * (-2*gamma*exerr1)

	a = i * (2 - delta + w01*y0 + wex0*(delta + gamma))
	b = i * (delta - 1 + w02*y0 - gamma*wex0 - w12*y1 - wex1*(delta + gamma))
	c = i * (w03*y0 - w13*y1 + gamma*wex1)
	d = i * (-w14*y1)
	
	return a,b,c,d,1,0,0,0,0,1,0,0,0,0,1,0
end
