--%  ft| BASIN_B0 sn| coexistence~curve~and~fixed~points n| #0 d| 1000 n| #1 d| 0.8 n| #2 d| 10 n| #3 d| 0.5 n| #4 d| 0.5 n| #5 d| 1.27 n| #6 d| 10e10 n| #7 d| 2000 n| #8 d| 1000 n| #9 d| 20 n| #10 d| 4000 n| #11 d| 6000 n| #12 d| 4000 n| #13 d| 6000
--%  ft| BASIN_B1 sn| different~algorithm~parameters n| #0 d| 1000 n| #1 d| 0.8 n| #2 d| 10 n| #3 d| 0.5 n| #4 d| 0.5 n| #5 d| 1.27 n| #6 d| 10e10 n| #7 d| 200 n| #8 d| 1000 n| #9 d| 20 n| #10 d| 4000 n| #11 d| 6000 n| #12 d| 4000 n| #13 d| 6000
--%  ft| BASIN_B0 sn| fig.~6 n| #0 d| 1000 n| #1 d| 0.8 n| #2 d| 10 n| #3 d| 1.15 n| #4 d| 0.5 n| #5 d| 2 n| #6 d| 10e10 n| #7 d| 2000 n| #8 d| 1000 n| #9 d| 50 n| #10 d| 0 n| #11 d| 10000 n| #12 d| 0 n| #13 d| 10000 
--@@
name = "multacc"
description = "multiplier-accelerator with jacobian"
type = "D"
parameters = {"i", "b", "gamma", "mu1", "mu2", "k"}
variables = {"Y", "Z"}

-- bidimensional with jacobian functioning

function f(i, b, gamma, mu1, mu2, k, Y, Z)

       eq = i/(1-b)

       devY = (Y - eq)/eq
       devZ = (Z - eq)/eq

       e1Y = Y + mu1 * (Y - eq)
       e2Y = Y + mu2 * (eq - Y)

       e1Z = Z + mu1 * (Z - eq)
       e2Z = Z + mu2 * (eq - Z)

       wY = 1 / (1 + gamma^2 * devY^2)
       wZ = 1 / (1 + gamma^2 * devZ^2)

       Y1 = i + b*(1+k) * (wY * e1Y + (1-wY) * e2Y)- k*b*(wZ * e1Z + (1-wZ) * e2Z)
       Z1 = Y

       return  Y1, Z1

end

function Jf(i, b, gamma, mu1, mu2, k, Y, Z, wY, wZ)


       eq = i/(1-b)

       devY = (Y - eq)/eq
       devZ = (Z - eq)/eq

       wY = 1 / (1 + gamma^2 * devY^2)
       wZ = 1 / (1 + gamma^2 * devZ^2)

       F = b*(1+k)*(wY*(mu1+mu2)*(2*wY-1) + (1 - mu2))
       G = -b*k*(wZ*(mu1+mu2)*(2*wZ-1) + (1 - mu2))

       return F,G,1,0

end
