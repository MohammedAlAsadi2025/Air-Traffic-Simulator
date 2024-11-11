package edu.curtin.saed.assignment1;

public class FlightRequest
{
    private Airport source;
    private Airport destination;

    public FlightRequest(Airport source, Airport destination)
    {
        this.source = source;
        this.destination = destination;
    }

    public Airport getSource()
    {
        return source;
    }

    public Airport getDestination()
    {
        return destination;
    }
}
