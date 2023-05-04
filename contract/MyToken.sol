pragma solidity ^0.8.9;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

contract MyToken is IERC20 {

    address private author;

    constructor(uint256 initialWeiPerToken) {
        author = msg.sender;
        weiPerToken = initialWeiPerToken;
    }

    /**
     * First go the standard ERC-20 functions.
     * The exchange functionality is implemented further.
     */
    
    uint256 private supply = 0;
    uint256 private weiPerToken; // exchange rate
    mapping(address => uint256) private balances;

    function totalSupply() override external view returns (uint256) {
        return supply;
    }

    function balanceOf(address account) override external view returns (uint256) {
        return balances[account];
    }

    function transfer(address to, uint256 amount) override external returns (bool) {
        address from = msg.sender;
        require(balances[from] >= amount, "Insufficient balance");

        balances[from] -= amount;
        balances[to] += amount;
        return true;
    }

    // Counldn't be bothered to implement these, allowance doesn't sound like such an important feature

    function allowance(address, address) override external pure returns (uint256) {
        revert("Allowance is not supported");
    }

    function approve(address, uint256) override external pure returns (bool) {
        revert("Allowance is not supported");
    }

    function transferFrom(address, address, uint256) override external pure returns (bool) {
        revert("Allowance is not supported");
    }

    /**
     * Here goes the exchanging code:
     */

    function buyTokens(uint256 tokensAmount) external payable returns (bool) {
        uint256 price = tokensAmount * weiPerToken;
        require(msg.value >= price, "Not enough Ether sent");
        balances[msg.sender] += tokensAmount;
        supply += tokensAmount;
        return true;
    }

    function sellTokens(uint256 tokensAmount, address payable recipient) external returns (bool) {
        require(recipient != address(0), "Invalid recipient");
        require(balances[recipient] >= tokensAmount, "Insufficient tokens");
        // note: will not refuse extra Ether if too much is sent
        uint256 price = tokensAmount * weiPerToken;
        balances[recipient] -= tokensAmount;
        supply -= tokensAmount;
        recipient.transfer(price);
        return true;
    }

    /**
     * Let's spark things up a little:
     */ 

    // Seems reasonable to allow everyone to monitor the changes in rate
    event ExchangeRateUpdated(uint256 newWeiPerToken); 

    function updateExchangeRate(uint256 newWeiPerToken) external {
        require(msg.sender == author, "Only author can update the exchange rate"); // :)
        weiPerToken = newWeiPerToken;
        emit ExchangeRateUpdated(newWeiPerToken);
    }

    function currentExchangeRate() external view returns (uint256) {
        return weiPerToken;
    }

}
