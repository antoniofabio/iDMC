#Usage example:
#
#> source("Grid.R")
#> g <- loadGrid("grid.dat") #substitute with your data file
#> plot(g)
#

Grid <- function(ranges, nr, nc, data) {
    ans <- list()
    m <- matrix(data, nr=nr, nc=nc, byrow=TRUE)
    m <- m[nr:1,]
    dX <- (ranges[2]-ranges[1])/nc
    dY <- (ranges[4]-ranges[3])/nr
    xr <- ranges[1:2]
    yr <- ranges[3:4]
    ans <- list(dX=dX, dY=dY, m=m, xr=xr, yr=yr)
    return(structure(ans, class="Grid"))
}

#Load an iDMC basins grid from a file f, and returns results as a Grid object
loadGrid <- function(f) {
    f <- gzfile(f, open="rb")
    ranges <- readBin(f, "double", 4, size=8, endian="big")
    nr <- readBin(f, "integer", 1, size=4, endian="big")
    nc <- readBin(f, "integer", 1, size=4, endian="big")
    data <- readBin(f, "integer", nr*nc, size=4, endian="big")
    close(f)
    return(Grid(ranges, nr, nc, data ))
}

plot.Grid <- function(this, palette, ...) {
    m <- t(this$m)
    lvls <- sort(unique(c(m))) #how many different levels?
    if(missing(palette))
        palette <- 1:length(lvls) #set a default palette
    dX <- this$dX
    dY <- this$dY
    x <- seq(this$yr[1], this$yr[2], length=nrow(m))
    y <- seq(this$xr[1], this$xr[2], length=ncol(m))
    image(x, y, m, col=palette, breaks=c(min(lvls)-1, lvls), ...)
}
