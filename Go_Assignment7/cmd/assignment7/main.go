package main

import (
	"github.com/gin-gonic/gin"

	"dryairship.org/iitkbucks/assignment7/controllers"
)

func main() {
	router := gin.Default()
	controllers.SetUpRoutes(router)

	err := router.Run(":8787")
	if err != nil {
		panic(err)
	}
}
