# schoolman-server
Backend for SchoolMan

SchoolMan is an open source School Manager, still on its initial release, but already offer interesting functionalities, including:

* A well looking training catalog, including all types of fees, that the public can easily browse to find interesting training offers matching their needs.
* The public can apply for a scheduled training session as both new and registered student.
* Once an application is received by email, the school will study it and if it is successful, the School will create an account for the student if new, enroll the student to the training session and that will automatically generate all the student's bills for that training, including supported payment methods details.
The student can browse all its training and corresponding fees, and proceed to payments.
* In the current release, all payment are managed outside the software. So once the money is received, the School will manually create a new Payment record for the bill in question. The next release will support Mobile Money payment (it is the standard in our region, Cameroon), Credit card payment, and PayPal. The creation of the Payment record will then be automated.
The School can monitor all ongoing training session, including student's financial status.

SchoolMan is open source, so you can download it, customize it, improve it the way you want. My customers pay for customization, and deployments.


The front-end is built with: React (TypeScript), NextJS, ApolloGraphQL (Client & Server), and ExpressJS (TypeScript).

The back-end is build with: Play Framework for Scala, Slick, and PostgreSQL.

Note that this is just the back-end part. You will need to deploy the [Frontend](https://github.com/acmoune/schoolman-web) part too.

Don't forget to edit the configuration files and create the Env variables to match your environment.

If you have any trouble deploying it, please create an issue.

Good luck!
