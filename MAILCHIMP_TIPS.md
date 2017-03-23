# Mandrill Tips

Our FlowUp server code uses [mandrill](https://mandrillapp.com/), a [Mailchimp](https://mailchimp.com) service to create email templates and send emails to our users when needed. All the credentials needed to access these services can be found in the company 1password account.

## Templates

The email templates we use are versioned in this repository but just as a backup. You can find all our templates [here](https://github.com/Karumi/FlowUpServer/tree/master/resources/mandrill%20templates). The real templates are configured and hosted by mandrill. You can modify them or create a new one from [this link](https://mandrillapp.com/templates).

When creating a new template or modifying a new one remember to update the local copy so we can keep a history of our templates.

## API Key

By default our server is configured using a development API key, but if needed you can access the production API key from the [mandrill settings page](https://mandrillapp.com/settings). Remember that the development API key will not send the email to the user, however the it will simulate a successful result if the email could be sent.

## Outbound emails

If you need to check the emails being sent to our users using the production API key you can find all the information needed in the [mandrill outbound page](https://mandrillapp.com/activity). From this page you can find any email sent and check if it was open or clicked, the email receiver, and much more info.