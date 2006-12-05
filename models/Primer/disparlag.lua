--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Oscillatory~convergence~to~a~fixed~point n| #0 d| 0.1 n| #1 d| 0.2 n| #2 d| 1 n| #3 d| 0.5 n| #4 d| 0 n| #5 d| 100 n| #6 d| 100 n| #7 d| tildez n| #8 d| tildep 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Quasiperiodic~orbit n| #0 d| 1 n| #1 d| 1 n| #2 d| 0.5 n| #3 d| 1 n| #4 d| 0 n| #5 d| 3000 n| #6 d| 3000 n| #7 d| tildez n| #8 d| tildep 
--@@
name = "Disparlag"
description = "See Model refs in user's guide"
type = "D"
parameters = {"b", "s"}
variables = {"tildez", "tildep"}

function f(b, s, tildez, tildep)

	tildez1 = tildep
	tildep1 = -s * tildez + (1-b) * tildep

   	return tildez1, tildep1

end

function Jf(b, s)

	return	0,	1,
			-s,	1-b

end




